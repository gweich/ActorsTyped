package weichhart.georg;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class MainSystemBehaviour extends AbstractBehavior<PerformativeMessages.Message> {

	public static final ServiceKey<PerformativeMessages.Message> msgServiceKey = ServiceKey
			.create(PerformativeMessages.Message.class, "msgService");
	
	static String FIRSTACTOR = "first-actor";

	public static Behavior<PerformativeMessages.Message> create() {
		
		return Behaviors.supervise(Behaviors.setup(MainSystemBehaviour::new)).onFailure(Exception.class,
				SupervisorStrategy.restart());
	}

	private MainSystemBehaviour(ActorContext<PerformativeMessages.Message> context) {
		super(context);
		// necessary? 
		context.getSystem().receptionist()
				.tell(Receptionist.register(MainSystemBehaviour.msgServiceKey, context.getSelf()));
	}

	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::msg)
				.onSignal(PreRestart.class, this::preRestart).onSignal(PostStop.class, this::postStop)
				.onSignal(ChildFailed.class, this::childFailed).onSignal(Terminated.class, this::childTerminated)
				.build();
	}

	private Behavior<PerformativeMessages.Message> msg(PerformativeMessages.Message m) {

		getContext().getLog().debug(getContext().getSelf().path().toString() + ":\r\n Msg Received: " + m.toString());

		if (m.getPerformative() == PerformativeType.REQUEST) {
			ActorRef<PerformativeMessages.Message> firstRef = getContext().spawn(PrintMyActorRefBehavior.create(),
					FIRSTACTOR);

			// to be notified when child dies or stops but not if supervision strategy. 
			getContext().watch(firstRef);

			getContext().getLog().debug("First Actor created:\r\n " + firstRef);

			firstRef.tell(PerformativeMessages.Message.newBuilder().setPerformative(PerformativeType.REQUEST)
					.setSubject("start actor").setSource(this.getContext().getSelf().path().toString())
					.setTxt("QWERTZUI").build());

		} else if (m.getPerformative() == PerformativeType.INFORM) {

			// contact my children not good for sending messages (in TypedActors):
			// The ActorContext has methods getChildren and getChild to retrieve the
			// ActorRef of started child actors in both Typed and Classic.
			// The type of the returned ActorRef is unknown, since different types can be
			// used for different children. Therefore, this is not a useful way to lookup
			// children when the purpose is to send messages to them.
			//			for (ActorRef<Void> child : getContext().getChildren()) {
			//				child.tell(m);
			//			}
			
			// search receptionist as a workaround; better: remember yourself.
			ActorRef<Object> resolvedSystemPath = akka.actor.typed.ActorRefResolver.get(getContext().getSystem())
					.resolveActorRef(getContext().getSelf().path().toSerializationFormat() + "/"+FIRSTACTOR);
			getContext().getLog().debug("inform?: " + resolvedSystemPath);
			if(resolvedSystemPath!=null)
				resolvedSystemPath.tell(m);
		
		}
		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> preRestart(PreRestart signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " preRestart " + signal.toString());

		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> postStop(PostStop signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " postStop " + signal.toString());

		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> childFailed(ChildFailed signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n childFailed " + signal);

		return Behaviors.stopped();
	}

	private Behavior<PerformativeMessages.Message> childTerminated(Terminated signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n childTerminated " + signal);

		return Behaviors.stopped();
	}
}

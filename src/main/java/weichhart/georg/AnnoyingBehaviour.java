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
import weichhart.georg.communication.PerformativeMessages;

public class AnnoyingBehaviour extends AbstractBehavior<PerformativeMessages.Message> {

	int AnoyCounter = 10;

	public static Behavior<PerformativeMessages.Message> create() {

		return Behaviors.supervise(Behaviors.setup(AnnoyingBehaviour::new)).onFailure(Exception.class,
				SupervisorStrategy.restart());

	}

	protected AnnoyingBehaviour(ActorContext<PerformativeMessages.Message> context) {
		super(context);
		context.getSystem().receptionist()
				.tell(Receptionist.register(MainSystemBehaviour.msgServiceKey, context.getSelf()));
	}

	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::anoy)
				.onSignal(PreRestart.class, this::preRestart).onSignal(PostStop.class, this::postStop)
				.onSignal(ChildFailed.class, this::childFailed).onSignal(Terminated.class, this::childTerminated)
				.build();
	}

	
	private Behavior<PerformativeMessages.Message> anoy(PerformativeMessages.Message m) {
		
		if(this.AnoyCounter-- >= 0) {
			
			ActorRef<Object> resolvedSystemPath = akka.actor.typed.ActorRefResolver.get(getContext().getSystem())
					.resolveActorRef("/user/**/"+MainSystemBehaviour.FIRSTACTOR);

			
			return Behaviors.same();			
		}
		
		// switch Behaviour
		return PrintMessageXtimesAndDie.create();
	}
	
	private Behavior<PerformativeMessages.Message> preRestart(PreRestart signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n PreRestart " + signal);

		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> postStop(PostStop signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n postStop " + signal);

		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> childFailed(ChildFailed signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n childFailed " + signal);

		return Behaviors.same();
	}

	private Behavior<PerformativeMessages.Message> childTerminated(Terminated signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "\r\n childTerminated " + signal);

		return Behaviors.same();
	}
	
}

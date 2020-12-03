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
import weichhart.georg.communication.PerformativeMessages.Message;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class PrintMyActorRefBehavior extends AbstractBehavior<PerformativeMessages.Message> {

	public static final ServiceKey<PerformativeMessages.Message> printServiceKey = ServiceKey
			.create(PerformativeMessages.Message.class, "printService");
	
	ActorRef<Receptionist.Listing> listingReceiver; 
	int myChildren=0;

	/** create behaviour with supervision strategy */
	public static Behavior<PerformativeMessages.Message> create() {
		Behavior<Message> b = Behaviors.setup(
				ctx -> {
					PrintMyActorRefBehavior b2 = new PrintMyActorRefBehavior(ctx);
					b2.registerAndSpawnChild();
					return b2;					
				});
		return Behaviors.supervise(b)
				.onFailure(Exception.class,
				SupervisorStrategy.restart());

	}

	protected PrintMyActorRefBehavior(ActorContext<PerformativeMessages.Message> context) {
		super(context);
	}

	protected PrintMyActorRefBehavior registerAndSpawnChild() {
		getContext().getSystem().receptionist().tell(Receptionist.register(printServiceKey, getContext().getSelf()));
		getContext().getSystem().receptionist()
				.tell(Receptionist.register(MainSystemBehaviour.msgServiceKey, getContext().getSelf()));
		/// in constructore TODO test ! 
		listingReceiver = getContext().spawn(ListingReceiver.create(getContext().getSelf()), "myListings");

		return this;
	}
	
	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::printIt)
				.onSignal(PreRestart.class, this::preRestart).onSignal(PostStop.class, this::postStop)
				.onSignal(ChildFailed.class, this::childFailed).onSignal(Terminated.class, this::childTerminated)				
				.build();
	}

	protected Behavior<PerformativeMessages.Message> printIt(PerformativeMessages.Message m) {

		getContext().getLog().debug(getContext().getSelf().path().name() + ": Msg Received:\r\n" + m.toString());

		if (m.getPerformative() == PerformativeMessages.Message.PerformativeType.REQUEST) {

			for (int i=0;i<6;++i) {
				ActorRef<PerformativeMessages.Message> secondRef = getContext().spawn(PrintMessageXtimesAndDie.create(),
						"child-actor-"+i);
				myChildren++;
				getContext().watch(secondRef);
	
				getContext().getLog().debug(getContext().getSelf().path().name() + ": Second: " + secondRef);
	
				secondRef.tell(m);
			}
			
		} else if (m.getPerformative() == PerformativeMessages.Message.PerformativeType.INFORM) {
			getContext().getLog().debug(getContext().getSelf().path().name() + ":INFORM");

			if(!m.getSubject().equals(ListingReceiver.SUBJECT_LISTING)) {
				// search all actors registered; the listingReceiver will forward the typed ActorRef using a PROPOSE
				// includes this actor
				getContext().getSystem().receptionist().tell(
						Receptionist.find(printServiceKey, listingReceiver));
			} else {
			
				ActorRef<PerformativeMessages.Message> resolvedSystemPath = akka.actor.typed.ActorRefResolver.get(getContext().getSystem())
						.resolveActorRef(m.getTxt());
				
				getContext().getLog().info(m.getPerformative().getValueDescriptor().getName());
				// would trigger the above INFORM / Listing sequence again
				if(resolvedSystemPath.compareTo(getContext().getSelf())!=0) {
					for(int i=0;i<6;++i)
						resolvedSystemPath.tell(PerformativeMessages.Message.newBuilder()
								.setPerformative(PerformativeType.INFORM)
								.setSource(getContext().getSelf().path().toSerializationFormat())
								.setSubject("do something")
								.setTxt("ABC").build());
				}
			}
			
		} 
		return Behaviors.same();
	}
	
	Behavior<PerformativeMessages.Message> preRestart(PreRestart signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " preRestart\r\n" + signal);

		return Behaviors.same();
	}

	Behavior<PerformativeMessages.Message> postStop(PostStop signal) {
		getContext().getLog().debug(getContext().getSelf().path().name() + " postStop\r\n" + signal);

		getContext().getSystem().receptionist().tell(Receptionist.deregister(printServiceKey, getContext().getSelf()));
		getContext().getSystem().receptionist().tell(Receptionist.deregister(MainSystemBehaviour.msgServiceKey, getContext().getSelf()));
				
		return Behaviors.same();
	}

	Behavior<PerformativeMessages.Message> childFailed(ChildFailed signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " childFailed\r\n" + signal);
		if(--myChildren<=0)
			return Behaviors.stopped();
		return Behaviors.same();
	}

	Behavior<PerformativeMessages.Message> childTerminated(Terminated signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " childTerminated\r\n" + signal);
		if(--myChildren<=0)
			return Behaviors.stopped();
		return Behaviors.same();
	}

}

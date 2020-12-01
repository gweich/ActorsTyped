package weichhart.georg;

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
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class PrintMessageXtimesAndDie extends AbstractBehavior<PerformativeMessages.Message> {
	
	int max = 3;
	
	int myChildren = 0;


	/** create behaviour with restart strategy on failure 
	 *  TODO: init max in create -> was passiert bei restart? 
	 * */
	public static Behavior<PerformativeMessages.Message> create() {
		return Behaviors.supervise(Behaviors.setup(PrintMessageXtimesAndDie::new))
				.onFailure(NullPointerException.class, SupervisorStrategy.restart());
	}
	
	/** register with Receptionist this behaviour */
	private PrintMessageXtimesAndDie(ActorContext<PerformativeMessages.Message> context) {
		super(context);
		context.getSystem().receptionist().tell(
				Receptionist.register(MainSystemBehaviour.msgServiceKey, context.getSelf()));
		context.getSystem().receptionist().tell(
				Receptionist.register(PrintMyActorRefBehavior.printServiceKey, context.getSelf()));

	}


	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::doIt)
				.onSignal(PreRestart.class, this::preRestart).onSignal(PostStop.class, this::postStop)
				.onSignal(ChildFailed.class, this::childFailed).onSignal(Terminated.class, this::childTerminated)				
				.build();
	}
	

	protected Behavior<PerformativeMessages.Message> doIt(PerformativeMessages.Message m) {
		
		
		getContext().getLog().debug(getContext().getSelf().path().name() + ":Msg ("+max+"): " + m.getPerformative().getValueDescriptor().getName());

		if (m.getPerformative() == PerformativeMessages.Message.PerformativeType.INFORM) {
			if (m.getSubject().equals("max")) {
				max = Integer.parseInt(m.getTxt());
			}
		} else if (m.getPerformative() == PerformativeMessages.Message.PerformativeType.FAILURE) {
			// msg to self. switch behavior
			return PrintMessageXtimesAndDie2.create();			
		}

		if(max-- >= 0) {
			// nop
		}
		
		if(max == 0) {
			throw new NullPointerException("Oh nooo " + getContext().getSelf().path().name());
			//return Behaviors.stopped();
		}
		
		return Behaviors.same();
	}
	
	/** switch to other behaviour; do not deregister the service 
	 * 
	 * 
	 * Note that PostStop is not emitted for a restart, so typically you need to handle both PreRestart and PostStop to cleanup resources.
	 * */
	Behavior<PerformativeMessages.Message> preRestart(PreRestart signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " preRestart\r\n" + signal + " max " + max);

		
		getContext().getSelf().tell(PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.FAILURE)
				.setSource(getContext().getSelf().path().toSerializationFormat())
				.setSubject("msg to self - trigger next behavior")
				.setTxt("failed - do next behavior").build());
		
		// switch to other Behaviour here is not working as expected
		//return PrintMessageXtimesAndDie2.create();
		return this;
	}

	/** this should not be called; because in preRestart we switch to another behavior */
	Behavior<PerformativeMessages.Message> postStop(PostStop signal) {
		getContext().getLog().debug(getContext().getSelf().path().name() + " postStop\r\n" + signal + " max " + max);

		getContext().getSystem().receptionist().tell(Receptionist.deregister(PrintMyActorRefBehavior.printServiceKey, getContext().getSelf()));
		getContext().getSystem().receptionist().tell(Receptionist.deregister(MainSystemBehaviour.msgServiceKey, getContext().getSelf()));
			
		// switch to other Behaviour does not make sense.
		return this;
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

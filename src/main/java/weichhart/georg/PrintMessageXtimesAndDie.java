package weichhart.georg;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class PrintMessageXtimesAndDie extends PrintMyActorRefBehavior {
	
	int max = 3;

	/** create behaviour with restart strategy on failure */
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
				Receptionist.register(printServiceKey, context.getSelf()));

	}

	@Override
	protected Behavior<PerformativeMessages.Message> printIt(PerformativeMessages.Message m) {
		
		
		getContext().getLog().debug(getContext().getSelf().path().name() + ":Msg ("+max+"): " + m.getPerformative().getValueDescriptor().getName());

		if (m.getPerformative() == PerformativeMessages.Message.PerformativeType.INFORM) {
			if (m.getSubject().equals("max")) {
				max = Integer.parseInt(m.getTxt());
			}
		}

		if(max-- >= 0) {
			// nop
		}
		
		if(max == 0) {
			throw new NullPointerException("Oh nooo " + getContext().getSelf().path().name());
		}
		
		if(max <=-1) {
			// there is a 50/50 change of another exception where we do not restart
			// and a proper stopping
			if(Math.random()>0.5)
				throw new ArrayIndexOutOfBoundsException("Again :-(" + getContext().getSelf().path().name());
			else {
				getContext().getLog().debug(getContext().getSelf().path().name() + " stopping ");
				return Behaviors.stopped();
			}
		}
		return Behaviors.same();
	}
	
	Behavior<PerformativeMessages.Message> preRestart(PreRestart signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " preRestart\r\n" + signal);
		getContext().getLog().debug(""+max);

		// sending future self a message about new max value
		getContext().getSelf().tell(PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFORM)
				.setSource(getContext().getSelf().path().toSerializationFormat())
				.setSubject("max")
				.setTxt("0").build());
		// TODO: other Behaviour
		return Behaviors.same();
	}
	
	Behavior<PerformativeMessages.Message> postStop(PostStop signal) {

		getContext().getLog().debug(getContext().getSelf().path().name() + " postStop\r\n" + signal);

		return Behaviors.same();
	}

}

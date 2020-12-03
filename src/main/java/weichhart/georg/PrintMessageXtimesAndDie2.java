package weichhart.georg;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import weichhart.georg.communication.PerformativeMessages;

public class PrintMessageXtimesAndDie2 extends PrintMyActorRefBehavior {

	
	/** create behaviour with restart strategy on failure 
	 *  TODO: init max in create -> was passiert bei restart? 
	 * */
	public static Behavior<PerformativeMessages.Message> create() {
		return Behaviors.setup(PrintMessageXtimesAndDie2::new);
	}
	
	private PrintMessageXtimesAndDie2(ActorContext<PerformativeMessages.Message> context) {
		super(context);
		getContext().getLog().debug(getContext().getSelf().path().name() + " created PrintMessageXtimesAndDie2 ");
	}
	
	
	@Override
	protected Behavior<PerformativeMessages.Message> printIt(PerformativeMessages.Message m) {

		getContext().getLog().debug(getContext().getSelf().path().name() + "  print it PrintMessageXtimesAndDie2");
		// there is a 50/50 change of another exception where we do not restart
		// and a proper stopping
		if(Math.random()<0.3)
			throw new ArrayIndexOutOfBoundsException("Again :-(" + getContext().getSelf().path().name());
		else {
			if(Math.random()<0.4)
				// and what NullPointerException
				throw new NullPointerException("Again a NPE ;-( " + getContext().getSelf().path().name());
			else
				getContext().getLog().debug(getContext().getSelf().path().name() + " stopping ");
			return Behaviors.stopped();
		}
	}
}

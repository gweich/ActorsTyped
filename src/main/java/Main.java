import akka.actor.typed.ActorSystem;
import weichhart.georg.MainSystemBehaviour;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class Main {

	public static void main(String[] args) {

		ActorSystem<PerformativeMessages.Message> firstActorSystem = ActorSystem.create(MainSystemBehaviour.create(), "MyFIRSTActorSystem");
		
		// Logging of Configuration
		firstActorSystem.logConfiguration();
		
		
		firstActorSystem.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.REQUEST)
				.setSubject("start actor")
				.setSource(firstActorSystem.path().toSerializationFormat())
				.setTxt("!\"§$%&/()").build()
				);
		
		// akka mechanism? 
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {} // we don't care if we are interrupted earlier
		
		firstActorSystem.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFORM)
				.setSubject("infrom actor to do something")
				.setSource(firstActorSystem.path().toSerializationFormat())
				.setTxt("!\"§$%&/()").build()
				);
		
	}

}

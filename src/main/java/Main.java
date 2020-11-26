import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import weichhart.georg.MainSystemBehaviour;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class Main {

	public static void main(String[] args) {

		ActorSystem<PerformativeMessages.Message> firstActorSystem = ActorSystem.create(MainSystemBehaviour.create(), "MyFIRSTActorSystem");
		
		// Logging of Configuration
		firstActorSystem.logConfiguration();
		
		ActorRef<Object> resolvedSystemPath = akka.actor.typed.ActorRefResolver.get(firstActorSystem).resolveActorRef(firstActorSystem.path().toSerializationFormat());
		
		firstActorSystem.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.REQUEST)
				.setSubject("start actor")
				.setSource(resolvedSystemPath.path().toString())
				.setTxt("!\"§$%&/()").build()
				);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {} // we don't care if we are interrupted earlier
		
		firstActorSystem.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFORM)
				.setSubject("infrom actor")
				.setSource(resolvedSystemPath.path().toString())
				.setTxt("!\"§$%&/()").build()
				);
		
	}

}

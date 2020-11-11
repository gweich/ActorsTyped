import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import weichhart.georg.MainSystemBehaviour;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class Main {

	public static void main(String[] args) {

		ActorSystem<PerformativeMessages.Message> firstActorSystem = ActorSystem.create(MainSystemBehaviour.create(), "MyFIRSTActorSystem");
		
		ActorRef<Object> reseloved = akka.actor.typed.ActorRefResolver.get(firstActorSystem).resolveActorRef(firstActorSystem.path().toSerializationFormat());
		
		firstActorSystem.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFOM)
				.setSubject("inform actor")
				.setSource(reseloved.path().toString())
				.setTxt("!\"§$%&/()").build()
				);
	}

}

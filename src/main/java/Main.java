import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import weichhart.georg.MainBehaviour;

public class Main {

	public static void main(String[] args) {

		ActorRef<String> testSystem = ActorSystem.create(MainBehaviour.create(), "testSystem");
		testSystem.tell("start");
	}

}

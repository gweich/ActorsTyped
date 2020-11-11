package weichhart.georg;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class MainSystemBehaviour extends AbstractBehavior<PerformativeMessages.Message> {

	public static Behavior<PerformativeMessages.Message> create() {
		return Behaviors.setup(MainSystemBehaviour::new);
	}

	private MainSystemBehaviour(ActorContext<PerformativeMessages.Message> context) {
		super(context);
	}

	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::start).build();
	}

	private Behavior<PerformativeMessages.Message> start(PerformativeMessages.Message m) {

		System.out.println(getContext().getSelf().path().toString() + ": Msg Received: " + m.toString());

		ActorRef<PerformativeMessages.Message> firstRef = getContext().spawn(PrintMyActorRefActor.create(), "first-actor");

		System.out.println("First Actor created: " + firstRef);
		firstRef.tell(
				PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFOM)
				.setSubject("inform actor")
				.setSource(this.getContext().getSelf().path().toString())
				.setTxt("QWERTZUI").build()
				);
		return Behaviors.same();
	}
}

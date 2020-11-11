package weichhart.georg;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import weichhart.georg.communication.PerformativeMessages;

public class PrintMyActorRefActor extends AbstractBehavior<PerformativeMessages.Message> {

	public static Behavior<PerformativeMessages.Message> create() {
		return Behaviors.setup(PrintMyActorRefActor::new);
	}

	private PrintMyActorRefActor(ActorContext<PerformativeMessages.Message> context) {
		super(context);
	}

	@Override
	public Receive<PerformativeMessages.Message> createReceive() {
		return newReceiveBuilder().onMessage(PerformativeMessages.Message.class, this::printIt).build();
	}

	private Behavior<PerformativeMessages.Message> printIt(PerformativeMessages.Message m) {
		System.out.println(getContext().getSelf().path().toString() + ": Msg Received: " + m.toString());
		ActorRef<PerformativeMessages.Message> secondRef = getContext().spawn(Behaviors.empty(), "SecondActor");
		System.out.println("Second: " + secondRef);
		return this;
	}
}

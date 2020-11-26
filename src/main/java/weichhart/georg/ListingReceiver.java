package weichhart.georg;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import scala.collection.Iterator;
import weichhart.georg.communication.PerformativeMessages;
import weichhart.georg.communication.PerformativeMessages.Message;
import weichhart.georg.communication.PerformativeMessages.Message.PerformativeType;

public class ListingReceiver extends AbstractBehavior<Receptionist.Listing> {

	public ListingReceiver received; 
	public ActorRef<PerformativeMessages.Message> parent;
	public static String SUBJECT_LISTING = "listing";
	
	public static Behavior<Receptionist.Listing> create(ActorRef<PerformativeMessages.Message> parent) {

		Behavior<Receptionist.Listing> b = Behaviors.setup(ctx -> new ListingReceiver(ctx, parent));

		return Behaviors.supervise(b).onFailure(Exception.class,
				SupervisorStrategy.restart());
	}

	private ListingReceiver(ActorContext<Receptionist.Listing> context, ActorRef<PerformativeMessages.Message> parent) {
		super(context);
		this.parent = parent;
	}

	@Override
	public Receive<Receptionist.Listing> createReceive() {
		return newReceiveBuilder().onMessage(Receptionist.Listing.class, this::listing)
				.build();
	}
	
	Behavior<Receptionist.Listing> listing(Receptionist.Listing l) {
		@SuppressWarnings("unchecked")
		Iterator<ActorRef<PerformativeMessages.Message>> i = (Iterator<ActorRef<Message>>) ((scala.collection.immutable.Set)l.allServiceInstances(l.getKey())).iterator();
		while(i.hasNext()) {
			ActorRef<PerformativeMessages.Message> actor = i.next();
			// parent from create method. 
			parent.tell(PerformativeMessages.Message.newBuilder()
				.setPerformative(PerformativeType.INFORM)
				.setSubject(SUBJECT_LISTING)
				.setTxt(actor.path().toSerializationFormat())
				.setSource(getContext().getSelf().path().toSerializationFormat()).build()
				);
		}
		
		return Behaviors.same();
	}

}

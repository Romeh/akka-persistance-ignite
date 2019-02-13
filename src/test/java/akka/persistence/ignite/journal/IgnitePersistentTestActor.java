package akka.persistence.ignite.journal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;
import akka.persistence.SnapshotOffer;
import akka.persistence.journal.Tagged;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by MRomeh
 * simple persistent actor sample for testing
 */
@Slf4j
public class IgnitePersistentTestActor extends AbstractPersistentActor {

	private String id;
	private List<String> list;
	private int snapShotInterval = 1000;

	public IgnitePersistentTestActor(String id) {
		this.id = id;
		list = new ArrayList<>();
	}

	private void putCmd(String s) {
		if (s.startsWith("+")) {
			list.add(s.substring(1));
		}
		if (s.startsWith("-")) {
			list.remove(s.substring(1));
		}
	}

	@Override
	public String persistenceId() {
		return id;
	}

	@Override
	public Receive createReceiveRecover() {
		return receiveBuilder()
				.match(String.class, this::putCmd)
				.match(RecoveryCompleted.class, s -> log.debug("done"))
				.match(SnapshotOffer.class, ss -> list = (List<String>) ss.snapshot())
				.build();
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(String.class, c -> {
					final String data = c;
					if ("throw".equals(data)) {
						throw new RuntimeException("Test Exception");
					} else {
						persist(new Tagged(new TestEvent(data), Collections.singleton("testTag")), s -> {
							putCmd(data);
							if (sender() != ActorRef.noSender()) {
								sender().tell(s, self());
							}
							if (lastSequenceNr() % snapShotInterval == 0 && lastSequenceNr() != 0)
								// IMPORTANT: create a copy of snapshot because ExampleState is mutable
								saveSnapshot(Collections.unmodifiableList(list));
						});
					}
				})
				.matchEquals("print", s -> System.out.println(list.toString()))
				.build();
	}


}

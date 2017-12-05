package akka.persistence.ignite.journal;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import com.typesafe.config.ConfigFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;




/**
 * just simple test for event sourced persistent actor based into ignite journal
 */
public class IgniteJournalCacheTest {

    private Ignite ignite;
    private ActorSystem actorSystem;

    @Before
    public void init() {
        actorSystem = ActorSystem.create("test", ConfigFactory.parseResources("test.conf"));
        ignite = IgniteExtensionProvider.EXTENSION.get(actorSystem).getIgnite();
    }

    @After
    public void destroy() {
        actorSystem.terminate();
        ignite.close();
    }

    @Test
    public void testPersistentActorWIthIgnite() throws Exception {
        ActorRef actorRef = actorSystem.actorOf(Props.create(IgnitePersistentTestActor.class, "1"));
        actorRef.tell("+a", ActorRef.noSender());
        actorRef.tell("+b", ActorRef.noSender());
        actorRef.tell("+c", ActorRef.noSender());
        actorRef.tell("throw", ActorRef.noSender());

        Future<Object> future = Patterns.ask(actorRef, "-b", 1000);
        Await.result(future, Duration.create(1, TimeUnit.SECONDS));

        IgniteCache<Object, Object> cache = ignite.getOrCreateCache("akka-journal");
        Assert.assertEquals(cache.size(), 4);

        actorSystem.actorSelection("akka://test/user/**").tell("!!!", ActorRef.noSender());

        Await.result(actorSystem.terminate(), Duration.create(1, TimeUnit.SECONDS));
    }


}
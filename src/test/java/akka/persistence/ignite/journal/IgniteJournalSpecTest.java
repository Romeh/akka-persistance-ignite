package akka.persistence.ignite.journal;

import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.japi.journal.JavaJournalSpec;
import com.typesafe.config.ConfigFactory;
import org.junit.runner.RunWith;
import org.scalatest.junit.JUnitRunner;

/**
 * Created by MRomeh
 * journal store test based into akka persistence plugin TCK
 */

@RunWith(JUnitRunner.class)
public class IgniteJournalSpecTest extends JavaJournalSpec {

    public IgniteJournalSpecTest() {
        super(ConfigFactory.parseString("akka.persistence.journal.plugin = \"akka.persistence.journal.ignite\""));
    }

    @Override
    public void afterAll() {
        IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(system());
        extension.getIgnite().close();
        super.afterAll();

    }


}

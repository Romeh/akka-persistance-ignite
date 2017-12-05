package akka.persistence.ignite.journal;

import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.japi.snapshot.JavaSnapshotStoreSpec;
import com.typesafe.config.ConfigFactory;
import org.junit.runner.RunWith;
import org.scalatest.junit.JUnitRunner;

/**
 * Created by MRomeh
 * snapshot store test based into akka persistence plugin TCK
 */
@RunWith(JUnitRunner.class)
public class IgniteSnapshotSpecTest extends JavaSnapshotStoreSpec {


    public IgniteSnapshotSpecTest() {
        super(ConfigFactory.parseString("akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot.ignite\""));
    }

    @Override
    public void afterAll() {
        IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(system());
        extension.getIgnite().close();
        super.afterAll();

    }

}

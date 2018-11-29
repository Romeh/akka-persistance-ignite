package akka.persistence.ignite.journal;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.junit.runner.RunWith;
import org.scalatest.junit.JUnitRunner;

import com.typesafe.config.ConfigFactory;

import akka.persistence.ignite.extension.IgniteExtension;
import akka.persistence.ignite.extension.IgniteExtensionProvider;
import akka.persistence.japi.snapshot.JavaSnapshotStoreSpec;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by MRomeh
 * snapshot store test based into akka persistence plugin TCK
 */
@RunWith(JUnitRunner.class)
@Slf4j
public class IgniteSnapshotSpecTest extends JavaSnapshotStoreSpec {

	public IgniteSnapshotSpecTest() {
		super(ConfigFactory.parseString("akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot.ignite\""));
	}

	@Override
	public void beforeAll() {
		try {
			Files.delete(FileSystems.getDefault().getPath("data").toAbsolutePath());
		} catch (IOException e) {
			log.error("an exception has been thrown during cleaning data directory for apache ignite {}", e.getMessage());
		}
		super.beforeAll();
	}

	@Override
	public void afterAll() {
		IgniteExtension extension = IgniteExtensionProvider.EXTENSION.get(system());
		extension.getIgnite().close();
		super.afterAll();

	}

}

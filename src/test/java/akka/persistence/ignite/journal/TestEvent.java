package akka.persistence.ignite.journal;

import java.io.Serializable;
import java.nio.file.FileSystems;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author romeh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEvent implements Serializable {
	private String data;

	public static void main(String... args) {
		System.out.println(FileSystems.getDefault().getPath("test").toAbsolutePath());
	}
}

package akka.persistence.ignite.journal;

import java.util.Collection;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by MRomeh
 * the journal cache value object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalItem implements Binarylizable {
	@QuerySqlField(index = true)
	private long sequenceNr;
	@QuerySqlField(index = true)
	private String persistenceId;
	private byte[] payload;
	private Collection<String> tags;
	@QuerySqlField(index = true)
	private long timestamp;

	@Override
	public void writeBinary(BinaryWriter out) throws BinaryObjectException {
		out.writeString("persistenceId", persistenceId);
		out.writeLong("sequenceNr", sequenceNr);
		out.writeByteArray("payload", payload);
		if (tags != null && !tags.isEmpty()) {
			out.writeCollection("tags", tags);
		}
		out.writeLong("timestamp", timestamp);
	}

	@Override
	public void readBinary(BinaryReader in) throws BinaryObjectException {
		persistenceId = in.readString("persistenceId");
		sequenceNr = in.readLong("sequenceNr");
		payload = in.readByteArray("payload");
		tags = in.readCollection("tags");
		timestamp = in.readLong("timestamp");
	}
}
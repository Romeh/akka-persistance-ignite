package akka.persistence.ignite.journal;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class JournalItem implements Externalizable {
    @QuerySqlField(index = true)
    private long sequenceNr;
    @QuerySqlField(index = true)
    private String persistenceId;
    private byte[] payload;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(persistenceId);
		out.writeLong(sequenceNr);
		out.writeInt(payload.length);
		out.write(payload);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		persistenceId = in.readUTF();
		sequenceNr = in.readLong();
		payload = new byte[in.readInt()];
		in.readFully(payload);

	}
}
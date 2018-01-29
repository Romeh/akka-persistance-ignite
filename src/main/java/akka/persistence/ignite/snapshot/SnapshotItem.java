package akka.persistence.ignite.snapshot;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by MRomeh
 * the snapshot cache value object
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SnapshotItem implements Serializable {
    @QuerySqlField(index = true, descending = true)
    private long sequenceNr;
    @QuerySqlField(index = true)
    private String persistenceId;
    // @QuerySqlField(index = true,orderedGroups={@QuerySqlField.Group(name="group", order=1,descending = true)})
    @QuerySqlField(index = true, descending = true)
    private long timestamp;
    private byte[] payload;
}

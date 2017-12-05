package akka.persistence.ignite.snapshot;

import lombok.*;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

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

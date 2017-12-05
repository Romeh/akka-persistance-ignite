package akka.persistence.ignite.journal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

/**
 * Created by MRomeh
 * the journal cache value object
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JournalItem implements Serializable {
    @QuerySqlField(index = true)
    private long sequenceNr;
    @QuerySqlField(index = true)
    private String persistenceId;
    private byte[] payload;
}
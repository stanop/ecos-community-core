package ru.citeck.ecos.records;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class RecordRef {

    public static final String SOURCE_DELIMITER = "@";
    private static final String DEFAULT_SOURCE_ID = "";

    private final String sourceId;
    private final String id;

    public RecordRef(String sourceId, String id) {
        this.sourceId = sourceId;
        this.id = id;
    }

    @JsonCreator
    public RecordRef(String id) {
        int sourceDelimIdx = id.indexOf(SOURCE_DELIMITER);
        if (sourceDelimIdx != -1) {
            sourceId = id.substring(0, sourceDelimIdx);
            this.id = id.substring(sourceDelimIdx + 1);
        } else {
            this.sourceId = DEFAULT_SOURCE_ID;
            this.id = id;
        }
    }

    public RecordRef(NodeRef id) {
        this.sourceId = DEFAULT_SOURCE_ID;
        this.id = id.toString();
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecordRef that = (RecordRef) o;
        return Objects.equals(sourceId, that.sourceId)
            && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(sourceId);
        result = 31 * result + Objects.hashCode(id);
        return result;
    }

    @JsonValue
    @Override
    public String toString() {
        if (StringUtils.isEmpty(sourceId)) {
            return id;
        } else {
            return sourceId + SOURCE_DELIMITER + id;
        }
    }
}

package ru.citeck.ecos.repo;

import lombok.Getter;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Objects;

public class RemoteRef {

    private static final String LOCAL_SERVER_ID = "";
    private static final String REMOTE_DELIMITER = "@";

    private final String serverId;
    private final String localId;

    @Getter(lazy = true)
    private final NodeRef nodeRef = evalNodeRef();

    public RemoteRef(String serverId, NodeRef nodeRef) {
        this.serverId = serverId;
        this.localId = nodeRef.toString();
    }

    public RemoteRef(String id) {
        String[] tokens = id.split(REMOTE_DELIMITER);
        if (tokens.length == 1) {
            serverId = LOCAL_SERVER_ID;
            localId = tokens[0];
        } else {
            serverId = tokens[0];
            localId = tokens[1];
        }
    }

    public RemoteRef(NodeRef nodeRef) {
        this.serverId = LOCAL_SERVER_ID;
        this.localId = nodeRef.toString();
    }

    public boolean isLocal() {
        return LOCAL_SERVER_ID.equals(serverId);
    }

    public boolean isRemote() {
        return !isLocal();
    }

    public String getServerId() {
        return serverId;
    }

    public String getLocalId() {
        return localId;
    }

    private NodeRef evalNodeRef() {
        return new NodeRef(localId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteRef that = (RemoteRef) o;
        return Objects.equals(serverId, that.serverId)
            && Objects.equals(localId, that.localId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(serverId);
        result = 31 * result + Objects.hashCode(localId);
        return result;
    }

    @Override
    public String toString() {
        if (isLocal()) {
            return localId;
        } else {
            return serverId + REMOTE_DELIMITER + localId;
        }
    }
}

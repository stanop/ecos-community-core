package ru.citeck.ecos.repo;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Objects;

public class RemoteNodeRef {

    private static final String REMOTE_DELIMITER = "@";
    private static final String LOCAL_ID = "";

    private final String serverId;
    private final NodeRef nodeRef;

    public RemoteNodeRef(String serverId, NodeRef nodeRef) {
        this.serverId = serverId;
        this.nodeRef = nodeRef;
    }

    public RemoteNodeRef(String id) {
        String[] tokens = id.split(REMOTE_DELIMITER);
        if (tokens.length == 1) {
            serverId = LOCAL_ID;
            nodeRef = new NodeRef(tokens[0]);
        } else {
            serverId = tokens[0];
            nodeRef = new NodeRef(tokens[1]);
        }
    }

    public RemoteNodeRef(NodeRef nodeRef) {
        this.serverId = LOCAL_ID;
        this.nodeRef = nodeRef;
    }

    public boolean isLocal() {
        return LOCAL_ID.equals(serverId);
    }

    public boolean isRemote() {
        return !isLocal();
    }

    public String getServerId() {
        return serverId;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteNodeRef that = (RemoteNodeRef) o;
        return Objects.equals(serverId, that.serverId)
            && Objects.equals(nodeRef, that.nodeRef);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(serverId);
        result = 31 * result + Objects.hashCode(nodeRef);
        return result;
    }

    @Override
    public String toString() {
        if (isLocal()) {
            return nodeRef.toString();
        } else {
            return serverId + REMOTE_DELIMITER + nodeRef;
        }
    }
}

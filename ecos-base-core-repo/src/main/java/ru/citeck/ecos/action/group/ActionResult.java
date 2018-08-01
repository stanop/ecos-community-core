package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteRef;

public class ActionResult {

    private final RemoteRef remoteRef;
    private final ActionStatus status;

    public ActionResult(RemoteRef remoteRef, String statusId) {
        this.remoteRef = remoteRef;
        this.status = new ActionStatus(statusId);
    }

    public ActionResult(RemoteRef remoteRef, ActionStatus status) {
        this.remoteRef = remoteRef;
        this.status = status;
    }

    public RemoteRef getRemoteRef() {
        return remoteRef;
    }

    public ActionStatus getStatus() {
        return status;
    }
}

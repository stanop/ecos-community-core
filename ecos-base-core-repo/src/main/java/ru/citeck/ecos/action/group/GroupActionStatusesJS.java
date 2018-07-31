package ru.citeck.ecos.action.group;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public class GroupActionStatusesJS {

    private Map<RemoteRef, GroupActionResult> statusesByRemoteRef;
    private Map<NodeRef, GroupActionResult> statusesByRef;

    private ServiceRegistry serviceRegistry;
    private Scriptable scope;

    GroupActionStatusesJS(Map<RemoteRef, GroupActionResult> statuses,
                                 Scriptable scope, ServiceRegistry serviceRegistry) {

        this.statusesByRemoteRef = statuses;
        this.statusesByRef = new HashMap<>(statuses.size());
        statuses.forEach((k, v) -> statusesByRef.put(k.getNodeRef(), v));
        this.scope = scope;
        this.serviceRegistry = serviceRegistry;
    }

    public ScriptNode[] getNodes() {
        List<ScriptNode> result = statusesByRemoteRef.keySet()
                                          .stream()
                                          .filter(RemoteRef::isLocal)
                                          .map(ref -> new ScriptNode(ref.getNodeRef(), serviceRegistry, scope))
                                          .collect(Collectors.toList());

        return result.toArray(new ScriptNode[result.size()]);
    }

    public String getStatus(ScriptNode node) {
        return statusesByRef.get(node.getNodeRef()).getStatus();
    }

    public String getMessage(ScriptNode node) {
        return statusesByRef.get(node.getNodeRef()).getMessage();
    }

    public String getUrl(ScriptNode node) {
        return statusesByRef.get(node.getNodeRef()).getUrl();
    }
}

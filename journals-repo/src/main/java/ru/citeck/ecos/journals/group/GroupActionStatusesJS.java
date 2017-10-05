package ru.citeck.ecos.journals.group;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * @author Pavel Simonov
 */
public class GroupActionStatusesJS {

    private Map<NodeRef, GroupActionResult> statuses;
    private ServiceRegistry serviceRegistry;
    private Scriptable scope;

    public GroupActionStatusesJS(Map<NodeRef, GroupActionResult> statuses,
                                 Scriptable scope, ServiceRegistry serviceRegistry) {
        this.statuses = statuses;
        this.scope = scope;
        this.serviceRegistry = serviceRegistry;
    }

    public ScriptNode[] getNodes() {
        ScriptNode[] result = new ScriptNode[statuses.size()];
        int idx = 0;
        for (NodeRef ref : statuses.keySet()) {
            result[idx++] = new ScriptNode(ref, serviceRegistry, scope);
        }
        return result;
    }

    public String getStatus(ScriptNode node) {
        return statuses.get(node.getNodeRef()).getStatus();
    }

    public String getMessage(ScriptNode node) {
        return statuses.get(node.getNodeRef()).getMessage();
    }

    public String getUrl(ScriptNode node) {
        return statuses.get(node.getNodeRef()).getUrl();
    }
}

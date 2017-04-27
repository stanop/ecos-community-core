package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.action.node.NodeActionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionsService {

    private static final Log LOGGER = LogFactory.getLog(NodeActionsService.class);

    private List<NodeActionsProvider> providerList;

    public NodeActionsService() {
        providerList= new ArrayList<>();
    }

    public void addActionProvider(NodeActionsProvider actionsProvider) {
        providerList.add(actionsProvider);
    }

    public List<Map<String, String>> getNodeActions(NodeRef nodeRef) {
        List<Map<String, String>> result = new ArrayList<>();
        int id = 0;
        for (NodeActionsProvider provider : providerList) {
            List<NodeActionDefinition> list = provider.getNodeActions(nodeRef);
            for (NodeActionDefinition action : list) {
                action.setActionId(Integer.toString(id++));
                if (action.isValid()) {
                    result.add(action.getProperties());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : action.getProperties().entrySet()) {
                        sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("; ");
                    }
                    LOGGER.warn("Server action is invalid. Properties: " + sb.toString());
                }
            }
        }
        return result;
    }

}

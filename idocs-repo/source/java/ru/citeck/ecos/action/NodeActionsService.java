package ru.citeck.ecos.action;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author deathNC on 30.04.2016.
 */
public class NodeActionsService {

    private List<NodeActionsProvider> providerList;

    public NodeActionsService() {
        providerList= new ArrayList<>();
    }


    public void addActionProvider(NodeActionsProvider actionsProvider) {
        providerList.add(actionsProvider);
    }

    public List<Map<String, String>> getNodeActions(NodeRef nodeRef) {
        List<Map<String, String>> result = new ArrayList<>();
        for (NodeActionsProvider provider : providerList) {
            List<NodeActionDefinition> list = provider.getNodeActions(nodeRef);
            for (NodeActionDefinition action : list) {
                result.add(action.getProperties());
            }
        }
        return result;
    }

}

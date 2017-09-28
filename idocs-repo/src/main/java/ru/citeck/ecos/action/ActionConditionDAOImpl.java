package ru.citeck.ecos.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;


//TODO: association saving doesn't work

class ActionConditionDAOImpl implements ActionDAO, ConditionDAO {

    private NodeService nodeService;
    private ActionService actionService;
    private DictionaryService dictionaryService;
    private String actionNamespace;
    private String conditionNamespace;

    @Override
    public NodeRef save(Action action, NodeRef parent, QName childAssocType) {
        QName type = getActionTypeName(action);
        Map<QName, Serializable> properties = getActionProperties(action);
        return nodeService.createNode(parent, childAssocType, type, type, properties).getChildRef();
    }

    @Override
    public void save(Action action, NodeRef nodeRef) {
        ensureCorrectType(nodeRef, getActionTypeName(action));
        Map<QName, Serializable> properties = getActionProperties(action);
        nodeService.setProperties(nodeRef, properties);
    }

    @Override
    public NodeRef save(ActionCondition condition, NodeRef parent, QName childAssocType) {
        QName type = getConditionTypeName(condition);
        Map<QName, Serializable> properties = getConditionProperties(condition);
        return nodeService.createNode(parent, childAssocType, type, type, properties).getChildRef();
    }

    @Override
    public void save(ActionCondition condition, NodeRef nodeRef) {
        ensureCorrectType(nodeRef, getConditionTypeName(condition));
        Map<QName, Serializable> properties = getConditionProperties(condition);
        nodeService.setProperties(nodeRef, properties);
    }

    @Override
    public Action readAction(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        String name = convertTypeToName(type, actionNamespace);
        Map<String, Serializable> parameters = extractParameters(nodeRef, actionNamespace);
        
        return actionService.createAction(name, parameters);
    }

    @Override
    public ActionCondition readCondition(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        String name = convertTypeToName(type, conditionNamespace);
        Map<String, Serializable> parameters = extractParameters(nodeRef, conditionNamespace);

        return actionService.createActionCondition(name, parameters);
    }

    @Override
    public List<Action> readActions(NodeRef parent, QName childAssocType) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parent, childAssocType, RegexQNamePattern.MATCH_ALL);
        switch(childAssocs.size()) {
        case 0: return Collections.emptyList();
        case 1: return Collections.singletonList(readAction(childAssocs.get(0).getChildRef()));
        default:
            List<Action> actions = new ArrayList<>(childAssocs.size());
            for(ChildAssociationRef childAssoc : childAssocs) {
                actions.add(readAction(childAssoc.getChildRef()));
            }
            return Collections.unmodifiableList(actions);
        }
    }

    @Override
    public List<ActionCondition> readConditions(NodeRef parent, QName childAssocType) {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parent, childAssocType, RegexQNamePattern.MATCH_ALL);
        switch(childAssocs.size()) {
        case 0: return Collections.emptyList();
        case 1: return Collections.singletonList(readCondition(childAssocs.get(0).getChildRef()));
        default:
            List<ActionCondition> actions = new ArrayList<>(childAssocs.size());
            for(ChildAssociationRef childAssoc : childAssocs) {
                actions.add(readCondition(childAssoc.getChildRef()));
            }
            return Collections.unmodifiableList(actions);
        }
    }

    // common private staff
    
    private QName convertNameToType(String name, String namespaceURI) {
        QName type = QName.createQName(namespaceURI, name);
        TypeDefinition typeDef = dictionaryService.getType(type);
        if(typeDef == null) {
            throw new IllegalArgumentException("Type does not exist: " + type);
        }
        return type;
    }

    private String convertTypeToName(QName type, String namespaceURI) {
        if(!type.getNamespaceURI().equals(namespaceURI)) {
            throw new IllegalArgumentException("Expected namespace " + namespaceURI + ", but found " + type);
        }
        return type.getLocalName();
    }

    private void ensureCorrectType(NodeRef nodeRef, QName requiredType) {
        QName actualType = nodeService.getType(nodeRef);
        if(requiredType.equals(actualType)) {
            // everything is fine
        } else if(dictionaryService.isSubClass(requiredType, actualType)) {
            nodeService.setType(nodeRef, requiredType);
        } else {
            throw new IllegalArgumentException("Can not specialize type of node " + nodeRef + " to " + requiredType);
        }
    }

    private Map<String, Serializable> extractParameters(NodeRef nodeRef, String namespace) {
        Map<String, Serializable> parameters;

        QName type = nodeService.getType(nodeRef);
        String name = convertTypeToName(type, namespace);

        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        String prefix = name + ":";

        parameters = new HashMap<>(convertPropertiesToParameters(nodeProps, namespace, prefix));
        parameters.putAll(convertAssocsToParameters(nodeRef, namespace, prefix));

        return parameters;
    }

    private Map<QName, Serializable> convertParametersToProperties(
            Map<String, Serializable> parameters, String namespaceURI, String localNamePrefix) {
        Map<QName, Serializable> actionProperties = new HashMap<>(parameters.size());
        for(Map.Entry<String, Serializable> parameterEntry : parameters.entrySet()) {
            QName propertyName = QName.createQName(namespaceURI, localNamePrefix + parameterEntry.getKey());
            actionProperties.put(propertyName, parameterEntry.getValue());
        }
        return actionProperties;
    }


    private Map<String, Serializable> convertAssocsToParameters(NodeRef nodeRef, String namespaceURI, String prefix) {

        Map<String, Serializable> result = new HashMap<>();

        QName type = nodeService.getType(nodeRef);
        TypeDefinition typeDef = dictionaryService.getType(type);

        Map<QName, AssociationDefinition> associations = typeDef.getAssociations();
        for (Map.Entry<QName, AssociationDefinition> assocDef : associations.entrySet()) {

            QName assocName = assocDef.getKey();
            String parameterName = getParameterName(assocName, namespaceURI, prefix);

            if (parameterName != null) {

                List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, assocName);
                if (targetAssocs == null) targetAssocs = Collections.emptyList();

                if (assocDef.getValue().isTargetMany()) {
                    ArrayList<NodeRef> nodeRefs = new ArrayList<>(targetAssocs.size());
                    for (AssociationRef assocRef : targetAssocs) {
                        nodeRefs.add(assocRef.getTargetRef());
                    }
                    result.put(parameterName, nodeRefs);
                } else {
                    NodeRef targetRef = targetAssocs.isEmpty() ? null : targetAssocs.get(0).getTargetRef();
                    result.put(parameterName, targetRef);
                }
            }
        }
        return result;
    }

    private Map<String, Serializable> convertPropertiesToParameters(
            Map<QName, Serializable> properties, String namespaceURI, String localNamePrefix) {
        Map<String, Serializable> parameters = new HashMap<>(properties.size());
        for(Map.Entry<QName, Serializable> entry : properties.entrySet()) {
            String parameterName = getParameterName(entry.getKey(), namespaceURI, localNamePrefix);
            if (parameterName != null) {
                parameters.put(parameterName, entry.getValue());
            }
        }
        return parameters;
    }

    private String getParameterName(QName attributeName, String namespace, String prefix) {

        if (attributeName.getNamespaceURI().equals(namespace) &&
            attributeName.getLocalName().startsWith(prefix)) {

            return attributeName.getLocalName().substring(prefix.length());
        }
        return null;
    }

    // actions private staff
    
    private QName getActionTypeName(Action action) {
        String actionName = action.getActionDefinitionName();
        return convertNameToType(actionName, actionNamespace);
    }

    private Map<QName, Serializable> getActionProperties(Action action) {
        return convertParametersToProperties(
                action.getParameterValues(), 
                actionNamespace, action.getActionDefinitionName() + ":");
    }

    // conditions private staff
    
    private QName getConditionTypeName(ActionCondition condition) {
        String conditionName = condition.getActionConditionDefinitionName();
        return convertNameToType(conditionName, conditionNamespace);
    }

    private Map<QName, Serializable> getConditionProperties(ActionCondition condition) {
        return convertParametersToProperties(
                condition.getParameterValues(), 
                conditionNamespace, condition.getActionConditionDefinitionName() + ":");
    }
    
    // spring interface

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setConditionNamespace(String conditionNamespace) {
        this.conditionNamespace = conditionNamespace;
    }

    public void setActionNamespace(String actionNamespace) {
        this.actionNamespace = actionNamespace;
    }

}

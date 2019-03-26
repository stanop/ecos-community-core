package ru.citeck.ecos.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NodeUtils {

    private static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";

    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public String getDisplayName(NodeRef nodeRef) {

        if (nodeRef == null) {
            return null;
        }

        QName type = nodeService.getType(nodeRef);
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

        if (ContentModel.TYPE_PERSON.equals(type)) {

            String firstName = (String) props.get(ContentModel.PROP_FIRSTNAME);
            String lastName = (String) props.get(ContentModel.PROP_LASTNAME);

            StringBuilder result = new StringBuilder();
            if (StringUtils.isNotBlank(firstName)) {
                result.append(firstName);
            }
            if (StringUtils.isNotBlank(lastName)) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(lastName);
            }

            if (result.length() == 0) {
                String userName = (String) props.get(ContentModel.PROP_USERNAME);
                if (StringUtils.isNotBlank(userName)) {
                    result.append(userName);
                } else {
                    result.append(nodeRef.toString());
                }
            }

            return result.toString();

        } else if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {

            String displayName = (String) props.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
            String authorityName = (String) props.get(ContentModel.PROP_AUTHORITY_NAME);

            return StringUtils.isNotBlank(displayName) ? displayName : authorityName;

        } else {

            String title = (String) props.get(ContentModel.PROP_TITLE);
            String name = (String) props.get(ContentModel.PROP_NAME);

            return StringUtils.isNotBlank(title) ? title : name;
        }
    }

    /**
     * Check is node pending for delete or not exist
     *
     * @param nodeRef Node reference
     * @return Check result
     */
    public boolean isValidNode(NodeRef nodeRef) {
        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            return false;
        }
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
            return true;
        }
        return !TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES).contains(nodeRef);
    }

    /**
     * Get node by nodeRef or path
     */
    public NodeRef getNodeRef(String node) {

        if (NodeRef.isNodeRef(node)) {
            return new NodeRef(node);
        }

        NodeRef root = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> results = searchService.selectNodes(root, node, null,
                                                          namespaceService, false);
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Node not found by path: " + node);
        }
        return results.get(0);
    }

    /**
     * Get node property
     */
    public <T> T getProperty(NodeRef nodeRef, QName propName) {
        @SuppressWarnings("unchecked")
        T result = (T) nodeService.getProperty(nodeRef, propName);
        return result;
    }

    public String getValidChildName(NodeRef parentRef, String name) {
        return getValidChildName(parentRef, ContentModel.ASSOC_CONTAINS, name);
    }

    public String getValidChildName(NodeRef parentRef, QName childAssoc, String name) {

        AssociationDefinition assoc = dictionaryService.getAssociation(childAssoc);

        if (!(assoc instanceof ChildAssociationDefinition) ||
                ((ChildAssociationDefinition) assoc).getDuplicateChildNamesAllowed()) {
            return name;
        }

        NodeRef child = nodeService.getChildByName(parentRef, childAssoc, name);
        if (child == null) {
            return name;
        }

        String extension = "." + FilenameUtils.getExtension(name);
        String nameWithoutExt = FilenameUtils.removeExtension(name);

        int index = 0;
        String newNameWithIndex;

        do {
            newNameWithIndex = nameWithoutExt + " (" + (++index) + ")" + extension;
            child = nodeService.getChildByName(parentRef, childAssoc, newNameWithIndex);
        } while (child != null);

        return newNameWithIndex;
    }

    public NodeRef createNode(NodeRef parentRef, QName type, QName childAssoc, Map<QName, Serializable> props) {

        String name = (String) props.get(ContentModel.PROP_NAME);
        if (name == null) {
            name = GUID.generate();
        }

        name = getValidChildName(parentRef, childAssoc, name);
        props.put(ContentModel.PROP_NAME, name);

        QName assocName = QName.createQNameWithValidLocalName(childAssoc.getNamespaceURI(), name);

        return nodeService.createNode(parentRef, childAssoc, assocName, type, props).getChildRef();
    }

    public boolean setAssocs(NodeRef nodeRef, Collection<NodeRef> targets, QName assocName) {

        if (!isValidNode(nodeRef) || assocName == null) {
            return false;
        }

        if (targets == null) {
            targets = Collections.emptySet();
        }

        Set<NodeRef> targetsSet = new HashSet<>(targets);

        AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);

        if (assocDef != null) {

            List<NodeRef> storedRefs = getAssocsImpl(nodeRef, assocDef, true);

            Set<NodeRef> toAdd = targetsSet.stream()
                                           .filter(r -> !storedRefs.contains(r))
                                           .collect(Collectors.toSet());
            Set<NodeRef> toRemove = storedRefs.stream()
                                              .filter(r -> !targetsSet.contains(r))
                                              .collect(Collectors.toSet());

            if (toAdd.size() > 0 || toRemove.size() > 0) {

                for (NodeRef removeRef : toRemove) {
                    nodeService.removeAssociation(nodeRef, removeRef, assocName);
                }
                for (NodeRef addRef : toAdd) {
                    nodeService.createAssociation(nodeRef, addRef, assocName);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Create association with specified node
     *
     * @return true if association was created or false if association already exists or arguments are invalid
     */
    public boolean createAssoc(NodeRef sourceRef, NodeRef targetRef, QName assocName) {

        if (!isValidNode(sourceRef) || !isValidNode(targetRef) || assocName == null) {
            return false;
        }
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
        if (assocDef != null) {
            List<NodeRef> storedRefs = getAssocsImpl(sourceRef, assocDef, true);
            if (!storedRefs.contains(targetRef)) {
                nodeService.createAssociation(sourceRef, targetRef, assocName);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove association with specified target node
     *
     * @return true if association was removed or false if association not exists or arguments are invalid
     */
    public boolean removeAssoc(NodeRef sourceRef, NodeRef targetRef, QName assocName) {

        if (!isValidNode(sourceRef) || !isValidNode(targetRef) || assocName == null) {
            return false;
        }
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
        if (assocDef != null) {
            List<NodeRef> storedRefs = getAssocsImpl(sourceRef, assocDef, true);
            if (storedRefs.contains(targetRef)) {
                nodeService.removeAssociation(sourceRef, targetRef, assocName);
                return true;
            }
        }
        return false;
    }

    /**
     * Get first node associated as target or child of specified sourceRef
     *
     * @return associated node
     */
    public Optional<NodeRef> getAssocTarget(NodeRef sourceRef, QName assocName) {
        return getAssocTargets(sourceRef, assocName).stream().findFirst();
    }

    /**
     * Get nodes associated as target or child of specified sourceRef
     *
     * @return list of associated nodes or empty list if arguments is not valid
     */
    public List<NodeRef> getAssocTargets(NodeRef sourceRef, QName assocName) {
        return getAssocs(sourceRef, assocName, true);
    }

    /**
     * Get nodes associated as source or parent of specified targetRef
     *
     * @return list of associated nodes or empty list if arguments is not valid
     */
    public List<NodeRef> getAssocSources(NodeRef targetRef, QName assocName) {
        return getAssocs(targetRef, assocName, false);
    }

    private List<NodeRef> getAssocs(NodeRef nodeRef, QName assocName, boolean nodeIsSource) {
        if (isValidNode(nodeRef) && assocName != null) {
            AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
            if (assocDef != null) {
                return getAssocsImpl(nodeRef, assocDef, nodeIsSource);
            }
        }
        return Collections.emptyList();
    }

    private List<NodeRef> getAssocsImpl(NodeRef nodeRef, AssociationDefinition assocDef, boolean nodeIsSource) {

        if (assocDef.isChild()) {

            List<ChildAssociationRef> assocsRefs;

            if (nodeIsSource) {
                assocsRefs = nodeService.getChildAssocs(nodeRef, assocDef.getName(), RegexQNamePattern.MATCH_ALL);
            } else {
                assocsRefs = nodeService.getParentAssocs(nodeRef, assocDef.getName(), RegexQNamePattern.MATCH_ALL);
            }

            return assocsRefs.stream()
                             .map(r -> nodeIsSource ? r.getChildRef() : r.getParentRef())
                             .collect(Collectors.toList());
        } else {

            List<AssociationRef> assocsRefs;

            if (nodeIsSource) {
                assocsRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
            } else {
                assocsRefs = nodeService.getSourceAssocs(nodeRef, assocDef.getName());
            }

            return assocsRefs.stream()
                             .map(r -> nodeIsSource ? r.getTargetRef() : r.getSourceRef())
                             .collect(Collectors.toList());
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        searchService = serviceRegistry.getSearchService();
        namespaceService = serviceRegistry.getNamespaceService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }
}

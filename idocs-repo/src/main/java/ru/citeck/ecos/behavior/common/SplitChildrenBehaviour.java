package ru.citeck.ecos.behavior.common;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.log4j.Logger;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Pavel Simonov
 */
public class SplitChildrenBehaviour implements OnCreateChildAssociationPolicy {

    private static final Logger logger = Logger.getLogger(SplitChildrenBehaviour.class);

    private NamespaceService namespaceService;
    private ServiceRegistry serviceRegistry;
    private PolicyComponent policyComponent;
    private SearchService searchService;
    private NodeService nodeService;
    private MimetypeService mimetypeService;

    private int order = 250;

    private NodeRef nodeRef;
    private String node;

    private SplitBehaviour splitBehaviour;

    private boolean enabled = true;

    private QName containerType = ContentModel.TYPE_FOLDER;
    private QName childAssocType = ContentModel.ASSOC_CONTAINS;

    private Map<Pair<NodeRef, String>, NodeRef> containersCache = new HashMap<>();

    public void init() {

        ParameterCheck.mandatoryString("node", node);
        ParameterCheck.mandatory("splitBehaviour", splitBehaviour);

        splitBehaviour.init(serviceRegistry);

        this.policyComponent.bindAssociationBehaviour(
                OnCreateChildAssociationPolicy.QNAME, containerType, childAssocType,
                new OrderedBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT, order)
        );
    }

    @Override
    public void onCreateChildAssociation(final ChildAssociationRef childAssociationRef, boolean b) {

        if (!enabled) return;

        final NodeRef parent = childAssociationRef.getParentRef();
        final NodeRef child = childAssociationRef.getChildRef();

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception {

                if (parent.equals(getNodeRef()) && nodeService.exists(child)
                        && !containerType.equals(nodeService.getType(child))) {

                    NodeRef actualParent = nodeService.getPrimaryParent(child).getParentRef();

                    if (parent.equals(actualParent)) {
                        moveChild(childAssociationRef);
                    }
                }
                return null;
            }
        });
    }

    private void moveChild(ChildAssociationRef assocRef) {

        NodeRef parent = assocRef.getParentRef();
        NodeRef child = assocRef.getChildRef();

        List<String> path = splitBehaviour.getPath(parent, child);

        if (!path.isEmpty()) {

            NodeRef destination = getContainer(parent, path, true);

            String name;
            if (childAssocType.equals(ContentModel.ASSOC_CONTAINS)) {
                name = getUniqueName(destination, child);
                nodeService.setProperty(child, ContentModel.PROP_NAME, name);
            } else {
                name = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
            }

            QName assocQName = QName.createQName(assocRef.getQName().getNamespaceURI(), name);
            nodeService.moveNode(child, destination, childAssocType, assocQName);

            splitBehaviour.onSuccess(parent, child);
        }
    }

    private String getUniqueName(NodeRef destination, NodeRef child) {
        String originalName = RepoUtils.getOriginalName(child, nodeService, mimetypeService);
        String extension = RepoUtils.getExtension(child, "", nodeService, mimetypeService);
        return RepoUtils.getUniqueName(destination, childAssocType, child,  originalName, extension, nodeService);
    }

    private NodeRef getContainer(NodeRef parent, List<String> path, boolean createIfNotExist) {
        NodeRef folderRef = parent;
        for (String name : path) {
            NodeRef child = getContainerByName(folderRef, name);
            if (child == null) {
                if (createIfNotExist) {
                    Map<QName, Serializable> props = new HashMap<>();
                    props.put(ContentModel.PROP_NAME, name);
                    child = nodeService.createNode(folderRef, childAssocType,
                                                   QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                                                   containerType, props).getChildRef();
                } else {
                    return null;
                }
            }
            folderRef = child;
        }
        return folderRef;
    }

    private NodeRef getContainerByName(NodeRef parent, String name) {

        Pair<NodeRef, String> data = new Pair<>(parent, name);

        NodeRef containerRef = containersCache.get(data);
        if (containerRef != null && nodeService.exists(containerRef)) {
            ChildAssociationRef containerParent = nodeService.getPrimaryParent(containerRef);
            if (Objects.equals(parent, containerParent.getParentRef())) {
                return containerRef;
            }
        }

        containerRef = queryContainerByName(parent, name);
        containersCache.put(data, containerRef);

        return containerRef;
    }

    private NodeRef queryContainerByName(NodeRef parent, String name) {

        String query = String.format("PARENT:\"%s\" AND TYPE:\"%s\" AND =cm\\:name:\"%s\"", parent, containerType, name);
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL);

        ResultSet results = null;
        try {
            results = searchService.query(searchParameters);
            if (results != null && results.length() > 0) {
                return results.getNodeRef(0);
            }
            return null;
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Children search failed. Query: \"" + query + "\"", e);
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private NodeRef getNodeRef() {
        if (nodeRef == null && node != null) {
            if (NodeRef.isNodeRef(node)) {
                nodeRef = new NodeRef(node);
            } else {
                NodeRef root = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                List<NodeRef> results = searchService.selectNodes(root, node, null, namespaceService, false);
                nodeRef = results.size() > 0 ? results.get(0) : null;
            }
        }
        return nodeRef;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSplitBehaviour(SplitBehaviour splitBehaviour) {
        this.splitBehaviour = splitBehaviour;
    }

    public void setContainerType(QName containerType) {
        this.containerType = containerType;
    }

    public void setChildAssocType(QName childAssocType) {
        this.childAssocType = childAssocType;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        nodeService = serviceRegistry.getNodeService();
        searchService = serviceRegistry.getSearchService();
        mimetypeService = serviceRegistry.getMimetypeService();
        namespaceService = serviceRegistry.getNamespaceService();
        policyComponent = (PolicyComponent) serviceRegistry.getService(AlfrescoServices.POLICY_COMPONENT);
    }

    public interface SplitBehaviour {
        void init(ServiceRegistry serviceRegistry);
        List<String> getPath(NodeRef parent, NodeRef node);
        void onSuccess(NodeRef parent, NodeRef node);
    }

    /*
    public class SimpleSplit implements SplitBehaviour {

        private int childrenPerParent = 500;
        private int depth = 2;

        @Override
        public List<String> getPath(NodeRef parent, NodeRef node) {

            int counter = getCounterValue(parent);
            int base = counter / childrenPerParent;

            List<String> path = new LinkedList<>();

            for (int i = 0; i < depth; i++) {
                path.add(0, String.valueOf(base % childrenPerParent));
                base /= childrenPerParent;
            }

            return path;
        }

        @Override
        public void onSuccess(NodeRef parent, NodeRef node) {
            //TODO: increase counter
        }

        private int getCounterValue(NodeRef parent) {
            //TODO: get counter from parent property or evaluate if not exists
            return 0;
        }

        public void setChildrenPerParent(int childrenPerParent) {
            if (childrenPerParent <= 0) {
                throw new IllegalArgumentException("Children count must be greater than zero");
            }
            this.childrenPerParent = childrenPerParent;
        }

        public void setDepth(int depth) {
            if (depth <= 0) {
                throw new IllegalArgumentException("Depth must be greater than zero");
            }
            this.depth = depth;
        }
    }*/

    /*public class ScriptSplit implements SplitBehaviour {

        private String script;

        @Override
        public List<String> getPath(NodeRef parent, NodeRef node) {

            if (StringUtils.isBlank(script)) {
                throw new IllegalStateException("Script is not specified!");
            }

            Map<String, Object> model = new HashMap<>();
            model.put("document", node);

            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) scriptService.executeScriptString(script, model);

            return result;
        }

        @Override
        public void onSuccess(NodeRef parent, NodeRef node) {

        }

        public void setScript(String script) {
            this.script = script;
        }
    }*/

    public static class DateSplit implements SplitBehaviour {

        public enum Depth {
            YEAR, YEAR_MONTH, YEAR_MONTH_DAY;
            final boolean hasYear = name().contains("YEAR");
            final boolean hasMonth = name().contains("MONTH");
            final boolean hasDay = name().contains("DAY");
        }

        private NodeService nodeService;

        private QName dateProperty = ContentModel.PROP_CREATED;
        private Boolean takeCurrentDateIfNull = null;

        private Depth depth = Depth.YEAR_MONTH_DAY;

        @Override
        public void init(ServiceRegistry serviceRegistry) {
            nodeService = serviceRegistry.getNodeService();
            if (takeCurrentDateIfNull == null) {
                takeCurrentDateIfNull = ContentModel.PROP_CREATED.equals(dateProperty);
            }
        }

        @Override
        public List<String> getPath(NodeRef parent, NodeRef node) {

            Date date = getDate(node);

            if (date != null) {

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                List<String> path = new ArrayList<>();

                if (depth.hasYear) path.add(String.valueOf(cal.get(Calendar.YEAR)));
                if (depth.hasMonth) path.add(String.valueOf(cal.get(Calendar.MONTH) + 1));
                if (depth.hasDay) path.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

                return path;
            }

            return Collections.emptyList();
        }

        @Override
        public void onSuccess(NodeRef parent, NodeRef node) {

        }

        private Date getDate(NodeRef node) {
            Date result = (Date) nodeService.getProperty(node, dateProperty);
            return result == null && takeCurrentDateIfNull ? new Date() : result;
        }

        public void setDateProperty(QName dateProperty) {
            this.dateProperty = dateProperty;
        }

        public void setTakeCurrentDateIfNull(boolean takeCurrentDateIfNull) {
            this.takeCurrentDateIfNull = takeCurrentDateIfNull;
        }

        public void setDepth(Depth depth) {
            this.depth = depth;
        }
    }
}

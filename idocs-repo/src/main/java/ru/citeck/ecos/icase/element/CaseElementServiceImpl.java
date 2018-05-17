/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.icase.element;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import ru.citeck.ecos.behavior.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import ru.citeck.ecos.icase.CaseConstants;
import ru.citeck.ecos.icase.element.config.ElementConfigDto;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.ExceptQNamePattern;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CaseElementServiceImpl extends AbstractLifecycleBean implements CaseElementService {

    private static final String KEY_COPIES = "case-copies";

    private static Log logger = LogFactory.getLog(CaseElementServiceImpl.class);

    private NodeService nodeService;
    private Map<QName, CaseElementDAO<ElementConfigDto>> strategies = new HashMap<>();
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private LazyNodeRef caseElementConfigRoot;

    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementAddPolicy> onCaseElementAddDelegate;
    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementUpdatePolicy> onCaseElementUpdateDelegate;
    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementRemovePolicy> onCaseElementRemoveDelegate;

    private Map<NodeRef, Optional<ElementConfigDto>> configByConfigNode = new ConcurrentHashMap<>();
    private Date caseElementConfigRootLastModified = null;

    private boolean initialized = false;

    public void init() {

        onCaseElementAddDelegate = policyComponent.registerClassPolicy(CaseElementPolicies.OnCaseElementAddPolicy.class);
        onCaseElementUpdateDelegate = policyComponent.registerClassPolicy(CaseElementPolicies.OnCaseElementUpdatePolicy.class);
        onCaseElementRemoveDelegate = policyComponent.registerClassPolicy(CaseElementPolicies.OnCaseElementRemovePolicy.class);

        // hook creating new configs:
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ICaseModel.TYPE_ELEMENT_CONFIG,
                new JavaBehaviour(this, "onElementConfigCreated", NotificationFrequency.TRANSACTION_COMMIT));
        // hook updating and deleting elements:
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ICaseModel.ASPECT_ELEMENT,
                new JavaBehaviour(this, "onElementUpdated", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ICaseModel.ASPECT_ELEMENT,
                new JavaBehaviour(this, "onElementUpdated", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ICaseModel.ASPECT_ELEMENT,
                new JavaBehaviour(this, "onElementUpdated", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ICaseModel.ASPECT_ELEMENT,
                new JavaBehaviour(this, "onElementDeleted", NotificationFrequency.EVERY_EVENT));
    }

    private List<ElementConfigDto> getAllElementConfigs() {
        return AuthenticationUtil.runAsSystem(this::getAllElementConfigsImpl);
    }

    private List<ElementConfigDto> getAllElementConfigsImpl() {

        NodeRef root = caseElementConfigRoot.getNodeRef();
        Date lastModified = (Date) nodeService.getProperty(root, ContentModel.PROP_MODIFIED);

        if (caseElementConfigRootLastModified == null
                || lastModified != null && lastModified.after(caseElementConfigRootLastModified)) {

            Collection<QName> elementConfigTypes =
                    dictionaryService.getSubTypes(ICaseModel.TYPE_ELEMENT_CONFIG, true);
            List<ChildAssociationRef> configAssocs =
                    nodeService.getChildAssocs(root, new HashSet<>(elementConfigTypes));

            List<NodeRef> nodeRefs = RepoUtils.getChildNodeRefs(configAssocs);

            configByConfigNode.clear();
            nodeRefs.forEach(this::getConfig);

            caseElementConfigRootLastModified = lastModified;
        }

        List<ElementConfigDto> configs = new ArrayList<>();
        configByConfigNode.values().forEach(c -> c.ifPresent(configs::add));

        return configs;
    }

    private List<ElementConfigDto> getElementConfigsByType(QName configType) {
        return getAllElementConfigs().stream()
                                     .filter(dto -> configType.equals(dto.getType()))
                                     .collect(Collectors.toList());
    }

    @Override
    public Optional<ElementConfigDto> getConfig(String configName) {
        return getAllElementConfigs().stream()
                                     .filter(dto -> configName.equals(dto.getName()))
                                     .findFirst();
    }

    @Override
    public Optional<ElementConfigDto> getConfig(NodeRef nodeRef) {
        Optional<ElementConfigDto> config = configByConfigNode.computeIfAbsent(nodeRef, ref ->
            AuthenticationUtil.runAsSystem(() -> createConfigDto(ref))
        );
        if (configByConfigNode.size() > 1000) {
            logger.warn("Configs cache size is too big. Possibly it is a memory leak. Last configRef: " + nodeRef);
            configByConfigNode.clear();
        }
        return config;
    }

    public CaseElementDAO<ElementConfigDto> getStrategy(ElementConfigDto config) {
        QName configType = config.getType();
        if (!strategies.containsKey(configType)) {
            throw new IllegalStateException("Case element config type is not supported: " + configType);
        }
        return strategies.get(configType);
    }

    public ElementConfigDto needConfig(String configName) {
        Optional<ElementConfigDto> config = getConfig(configName);
        if (!config.isPresent()) {
            throw new IllegalStateException("Necessary element config is missing: " + configName);
        }
        return config.get();
    }

    private List<String> getConfigNames(List<NodeRef> configs) {
        List<String> result = new ArrayList<>();
        configs.forEach(ref -> getConfig(ref).ifPresent(c -> result.add(c.getName())));
        return result;
    }

    @Override
    public List<String> getAllElementTypes() {
        return getAllElementConfigs().stream()
                                     .map(ElementConfigDto::getName)
                                     .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllElementTypes(NodeRef caseNodeRef) {
        List<NodeRef> configs = getElements(caseNodeRef, CaseConstants.ELEMENT_TYPES);
        return getConfigNames(configs);
    }

    @Override
    public List<NodeRef> getElements(NodeRef caseNodeRef, String configName) {
        exists(caseNodeRef);
        ElementConfigDto configNode = needConfig(configName);
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode);
        return strategy.get(caseNodeRef, configNode);
    }

    @Override
    public List<NodeRef> getCases(NodeRef nodeRef, String configName) {
        exists(nodeRef);
        ElementConfigDto configNode = needConfig(configName);
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode);
        return strategy.getCases(nodeRef, configNode);
    }

    @Override
    public void addElement(NodeRef nodeRef, NodeRef caseNodeRef,
                           String configName) throws AlfrescoRuntimeException {
        exists(nodeRef);
        ElementConfigDto configNode = needConfig(configName);
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode);
        strategy.add(nodeRef, caseNodeRef, configNode);
    }

    @Override
    public void addElements(Collection<NodeRef> elements, NodeRef caseNodeRef,
                            String elementType) throws AlfrescoRuntimeException {
        exists(caseNodeRef);
        ElementConfigDto configNode = needConfig(elementType);
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode);
        strategy.addAll(elements, caseNodeRef, configNode);
    }

    @Override
    public void removeElement(NodeRef nodeRef, NodeRef caseNodeRef, String configName)
            throws AlfrescoRuntimeException {
        exists(nodeRef);
        ElementConfigDto configNode = needConfig(configName);
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode);
        strategy.remove(nodeRef, caseNodeRef, configNode);
    }

    @Override
    public NodeRef destination(NodeRef caseNodeRef, String configName)
            throws AlfrescoRuntimeException {

        Optional<ElementConfigDto> configNode = getConfig(configName);
        if (!configNode.isPresent()) {
            throw new AlfrescoRuntimeException("Can not find config node by name: " + configName);
        }
        CaseElementDAO<ElementConfigDto> strategy = getStrategy(configNode.get());

        return strategy.destination(caseNodeRef, configNode.get());
    }

    @Override
    public void copyCaseToTemplate(NodeRef caseNodeRef, NodeRef templateRef) {

        exists(caseNodeRef);
        exists(templateRef);

        ElementConfigDto elementTypesConfig = needConfig(CaseConstants.ELEMENT_TYPES);
        CaseElementDAO<ElementConfigDto> elementTypesStrategy = getStrategy(elementTypesConfig);

        List<NodeRef> elementConfigs = elementTypesStrategy.get(caseNodeRef, elementTypesConfig);

        for (NodeRef configRef : elementConfigs) {
            if (elementTypesConfig.getNodeRef().equals(configRef)) {
                continue;
            }

            Optional<ElementConfigDto> configOpt = getConfig(configRef);

            if (configOpt.isPresent()) {

                ElementConfigDto config = configOpt.get();

                NodeRef elementType = getOrCreateElementType(templateRef, config);

                if (config.isCopyElements()) {
                    CaseElementDAO<ElementConfigDto> strategy = getStrategy(config);
                    strategy.copyElementsToTemplate(caseNodeRef, elementType, config);
                }
            }
        }

        registerElementCopy(caseNodeRef, templateRef);

        adjustCopies();
    }

    @Override
    public void copyTemplateToCase(NodeRef templateRef, NodeRef caseNodeRef) {

        exists(caseNodeRef);
        exists(templateRef);

        ElementConfigDto elementTypesConfig = needConfig(CaseConstants.ELEMENT_TYPES);
        CaseElementDAO<ElementConfigDto> elementTypesStrategy = getStrategy(elementTypesConfig);

        List<NodeRef> elementTypes = RepoUtils.getChildrenByAssoc(templateRef,
                                                                  ICaseTemplateModel.ASSOC_ELEMENT_TYPES,
                                                                  nodeService);
        for (NodeRef elementType : elementTypes) {

            ElementConfigDto config = getTemplateElementConfig(elementType);
            elementTypesStrategy.add(config.getNodeRef(), caseNodeRef, elementTypesConfig);

            if (config.isCopyElements()) {
                CaseElementDAO<ElementConfigDto> strategy = getStrategy(config);
                strategy.copyElementsFromTemplate(elementType, caseNodeRef, config);
            }
        }

        registerElementCopy(templateRef, caseNodeRef);

        adjustCopies();
    }

    private NodeRef getOrCreateElementType(NodeRef templateRef, ElementConfigDto config) {
        String configName = config.getName();
        NodeRef elementType = nodeService.getChildByName(templateRef,
                                                         ICaseTemplateModel.ASSOC_ELEMENT_TYPES,
                                                         configName);
        if (elementType == null) {
            elementType = nodeService.createNode(templateRef,
                    ICaseTemplateModel.ASSOC_ELEMENT_TYPES,
                    QName.createQName(ICaseTemplateModel.NAMESPACE, configName),
                    ICaseTemplateModel.TYPE_ELEMENT_TYPE).getChildRef();
            nodeService.createAssociation(elementType, config.getNodeRef(), ICaseTemplateModel.ASSOC_ELEMENT_CONFIG);
        }
        return elementType;
    }

    private ElementConfigDto getTemplateElementConfig(NodeRef elementType) {
        List<NodeRef> elementConfigs = RepoUtils.getTargetNodeRefs(elementType,
                                                                   ICaseTemplateModel.ASSOC_ELEMENT_CONFIG,
                                                                   nodeService);
        if (elementConfigs.size() == 0) {
            throw new IllegalStateException("Template element type does not contain " +
                                            "element config reference: " + elementType);
        }

        return getConfig(elementConfigs.get(0)).orElseThrow(() ->
                new IllegalArgumentException("nodeRef is not a config " + elementConfigs.get(0))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<NodeRef, NodeRef> getCopyMap() {
        return (Map<NodeRef, NodeRef>) AlfrescoTransactionSupport.getResource(KEY_COPIES);
    }

    /*package*/ void registerElementCopy(NodeRef original, NodeRef copy) {
        Map<NodeRef, NodeRef> copyMap = getCopyMap();
        if (copyMap == null) {
            copyMap = new HashMap<>();
            AlfrescoTransactionSupport.bindResource(KEY_COPIES, copyMap);
        }
        // the same original should not be copied twice
        NodeRef previousCopy = copyMap.put(original, copy);
        if (previousCopy != null) {
            throw new IllegalStateException("Each original element can only be copied once. Attemting to register second copy of: " + original);
        }
    }

    private void adjustCopies() {
        Map<NodeRef, NodeRef> copyMap = getCopyMap();
        if (copyMap == null) {
            return;
        }
        AlfrescoTransactionSupport.unbindResource(KEY_COPIES);

        // step 1: add children to copy map
        Queue<NodeRef> queue = new LinkedList<>();
        queue.addAll(copyMap.values());
        while (!queue.isEmpty()) {
            NodeRef node = queue.poll();
            List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(node);
            for (ChildAssociationRef childAssoc : childAssocs) {
                NodeRef childRef = childAssoc.getChildRef();
                List<AssociationRef> assocs = nodeService.getTargetAssocs(childRef, ContentModel.ASSOC_ORIGINAL);
                if (assocs != null && !assocs.isEmpty()) {
                    NodeRef original = assocs.get(0).getTargetRef();
                    copyMap.put(original, childRef);
                }
                queue.add(childRef);
            }
        }

        // step 2: adjust all links
        Collection<NodeRef> copies = copyMap.values();
        QNamePattern assocPattern = new ExceptQNamePattern(ContentModel.ASSOC_ORIGINAL);
        for (NodeRef copy : copies) {
            List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(copy, assocPattern);
            for (AssociationRef assoc : targetAssocs) {
                NodeRef targetOriginal = assoc.getTargetRef();
                NodeRef targetCopy = copyMap.get(targetOriginal);
                if (targetCopy == null) {
                    continue;
                }
                QName assocType = assoc.getTypeQName();
                nodeService.removeAssociation(copy, targetOriginal, assocType);
                nodeService.createAssociation(copy, targetCopy, assocType);
            }
        }
    }

    protected void exists(NodeRef nodeRef) throws AlfrescoRuntimeException {
        if (!nodeService.exists(nodeRef)) {
            throw new AlfrescoRuntimeException("Specified node reference does not exist. nodeRef=" + nodeRef);
        }
    }

    public void invokeOnCaseElementAdd(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        if (!nodeService.hasAspect(element, ICaseModel.ASPECT_ELEMENT)) {
            nodeService.addAspect(element, ICaseModel.ASPECT_ELEMENT, null);
        }
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementAddPolicy policy = onCaseElementAddDelegate.get(new HashSet<>(classes));
        policy.onCaseElementAdd(caseRef, element, config);
    }

    public void invokeOnCaseElementUpdate(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementUpdatePolicy policy = onCaseElementUpdateDelegate.get(new HashSet<>(classes));
        policy.onCaseElementUpdate(caseRef, element, config);
    }

    public void invokeOnCaseElementRemove(NodeRef caseRef, NodeRef element, ElementConfigDto config) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementRemovePolicy policy = onCaseElementRemoveDelegate.get(new HashSet<>(classes));
        policy.onCaseElementRemove(caseRef, element, config);
    }

    public void onElementConfigCreated(ChildAssociationRef childAssoc) {

        final NodeRef configNode = childAssoc.getChildRef();

        if (!nodeService.exists(configNode)) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing element config " + configNode + " of type " + nodeService.getType(configNode));
        }

        Optional<ElementConfigDto> configOpt = getConfig(configNode);

        configOpt.ifPresent(config -> {

            final CaseElementDAO<ElementConfigDto> strategy = getStrategy(config);

            if (strategy == null) {
                // assume, that this strategy is not yet registered
                // when it is registered, this method will be called again
                return;
            }
            AuthenticationUtil.runAsSystem(() -> {
                strategy.intializeBehaviours(config);
                return null;
            });
            // TODO support unbinding behaviours
        });
    }

    private void onCaseElementUpdated(NodeRef element) {
        if (!nodeService.exists(element)) {
            return;
        }

        boolean elementHasCases = false;

        // check all existing case element configurations:
        List<ElementConfigDto> configs = this.getAllElementConfigs();
        for (ElementConfigDto config : configs) {
            CaseElementDAO<ElementConfigDto> strategy = getStrategy(config);
            List<NodeRef> caseNodes = strategy.getCases(element, config);
            if (caseNodes.size() > 0) {
                elementHasCases = true;
            }
            for (NodeRef caseNode : caseNodes) {
                invokeOnCaseElementUpdate(caseNode, element, config);
            }
        }
        // if there was no case, it is not an element anymore
        if (!elementHasCases) {
            nodeService.removeAspect(element, ICaseModel.ASPECT_ELEMENT);
        }
    }

    public void onElementUpdated(NodeRef element, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        onCaseElementUpdated(element);
    }

    public void onElementUpdated(AssociationRef assocRef) {
        onCaseElementUpdated(assocRef.getSourceRef());
    }

    public void onElementDeleted(NodeRef element) {
        if (!nodeService.exists(element)) {
            return;
        }

        // check all existing case element configurations:
        List<ElementConfigDto> configs = this.getAllElementConfigs();
        for (ElementConfigDto config : configs) {
            CaseElementDAO<ElementConfigDto> strategy = getStrategy(config);
            List<NodeRef> caseNodes = strategy.getCases(element, config);
            for (NodeRef caseNode : caseNodes) {
                invokeOnCaseElementRemove(caseNode, element, config);
            }
        }
    }

    public void register(CaseElementDAO strategy) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering strategy " + strategy);
        }
        QName configType = strategy.getElementConfigType();
        @SuppressWarnings("unchecked")
        CaseElementDAO<ElementConfigDto> elementDAO = (CaseElementDAO<ElementConfigDto>) strategy;
        strategies.put(configType, elementDAO);
        if (initialized) {
            init(strategy);
        }
    }

    private void init(CaseElementDAO strategy) {
        QName configType = strategy.getElementConfigType();
        List<ElementConfigDto> configs = getElementConfigsByType(configType);
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing strategy " + strategy + ": Found " + configs.size() + " case element config nodes of this type");
        }
        for (ElementConfigDto config : configs) {
            onElementConfigCreated(nodeService.getPrimaryParent(config.getNodeRef()));
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        // initialize all previously registered strategies:
        this.initialized = true;
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing " + strategies.size() + " case element strategies");
        }
        for (CaseElementDAO strategy : strategies.values()) {
            init(strategy);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // nothing to do
    }

    private Optional<ElementConfigDto> createConfigDto(NodeRef configRef) {

        QName type = nodeService.getType(configRef);
        if (!dictionaryService.isSubClass(type, ICaseModel.TYPE_ELEMENT_CONFIG)) {
            return Optional.empty();
        }

        CaseElementDAO<ElementConfigDto> dao = strategies.get(type);

        ElementConfigDto config;
        if (dao != null) {
            config = dao.createConfig(configRef);
        } else {
            return Optional.empty();
        }

        config.setType(nodeService.getType(configRef));

        Map<QName, Serializable> props = nodeService.getProperties(configRef);

        config.setName((String) props.get(ContentModel.PROP_NAME));
        config.setCaseClass((QName) props.get(ICaseModel.PROP_CASE_CLASS));
        config.setElementType((QName) props.get(ICaseModel.PROP_ELEMENT_TYPE));
        config.setCopyElements((Boolean) props.get(ICaseModel.PROP_COPY_ELEMENTS));
        config.setFolderName((String) props.get(ICaseModel.PROP_FOLDER_NAME));
        config.setFolderType((QName) props.get(ICaseModel.PROP_FOLDER_TYPE));
        config.setFolderAssocName((QName) props.get(ICaseModel.PROP_FOLDER_ASSOC_TYPE));
        config.setCreateSubcase((Boolean) props.get(ICaseModel.PROP_CREATE_SUBCASE));
        config.setRemoveSubcase((Boolean) props.get(ICaseModel.PROP_REMOVE_SUBCASE));
        config.setRemoveEmptySubcase((Boolean) props.get(ICaseModel.PROP_REMOVE_EMPTY_SUBCASE));
        config.setSubcaseType((QName) props.get(ICaseModel.PROP_SUBCASE_TYPE));
        config.setSubcaseAssoc((QName) props.get(ICaseModel.PROP_SUBCASE_ASSOC));

        return Optional.of(config);
    }

    public void clearCache() {
        configByConfigNode.clear();
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setCaseElementConfigRoot(LazyNodeRef caseElementConfigRoot) {
        this.caseElementConfigRoot = caseElementConfigRoot;
    }
}

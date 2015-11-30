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
package ru.citeck.ecos.icase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
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

import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.ExceptQNamePattern;
import ru.citeck.ecos.utils.LazyNodeRef;
import ru.citeck.ecos.utils.RepoUtils;

public class CaseElementServiceImpl extends AbstractLifecycleBean implements CaseElementService {
    
    private static final String KEY_COPIES = "case-copies";

    private static Log logger = LogFactory.getLog(CaseElementServiceImpl.class);

    private NodeService nodeService;
    private Map<QName, CaseElementDAO> strategies = new HashMap<>();
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private LazyNodeRef caseElementConfigRoot;
    
    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementAddPolicy> onCaseElementAddDelegate;
    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementUpdatePolicy> onCaseElementUpdateDelegate;
    private ClassPolicyDelegate<CaseElementPolicies.OnCaseElementRemovePolicy> onCaseElementRemoveDelegate;

    private boolean initialized = false;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }
    
    public void setCaseElementConfigRoot(LazyNodeRef caseElementConfigRoot) {
        this.caseElementConfigRoot = caseElementConfigRoot;
    }

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
        policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ICaseModel.ASPECT_ELEMENT, 
                new JavaBehaviour(this, "onElementDeleted", NotificationFrequency.EVERY_EVENT));
    }

    private List<NodeRef> getAllElementConfigs() {
        NodeRef root = caseElementConfigRoot.getNodeRef();
        Collection<QName> elementConfigTypes = dictionaryService.getSubTypes(ICaseModel.TYPE_ELEMENT_CONFIG, true);
        List<ChildAssociationRef> configAssocs = nodeService.getChildAssocs(root, new HashSet<>(elementConfigTypes));
        return RepoUtils.getChildNodeRefs(configAssocs);
    }
    
    private List<NodeRef> getElementConfigsByType(QName configType) {
        NodeRef root = caseElementConfigRoot.getNodeRef();
        List<ChildAssociationRef> configAssocs = nodeService.getChildAssocs(root, Collections.singleton(configType));
        return RepoUtils.getChildNodeRefs(configAssocs);
    }
    
    /*package*/ NodeRef getConfig(String configName) {
        NodeRef root = caseElementConfigRoot.getNodeRef();
        NodeRef config = nodeService.getChildByName(root, ContentModel.ASSOC_CONTAINS, configName);
        if(config == null) {
            return null;
        }
        if(!RepoUtils.isSubType(config, ICaseModel.TYPE_ELEMENT_CONFIG, nodeService, dictionaryService)) {
            return null;
        }
        return config;
    }
    
    /*package*/ CaseElementDAO getStrategy(NodeRef config) {
        QName configType = nodeService.getType(config);
        if(!strategies.containsKey(configType)) {
            throw new IllegalStateException("Case element config type is not supported: " + configType);
        }
        return strategies.get(configType);
    }
    
    /*package*/ NodeRef needConfig(String configName) {
        NodeRef config = getConfig(configName);
        if(config == null)
            throw new IllegalStateException("Necessary element config is missing: " + configName);
        return config;
    }

    private List<String> getConfigNames(List<NodeRef> configs) {
        List<String> configNames = new ArrayList<>(configs.size());
        for(NodeRef config : configs) {
            configNames.add(getConfigName(config));
        }
        return configNames;
    }

    private String getConfigName(NodeRef config) {
        return (String) nodeService.getProperty(config, ContentModel.PROP_NAME);
    }

    @Override
    public List<String> getAllElementTypes() {
        List<NodeRef> configs = getAllElementConfigs();
        return getConfigNames(configs);
    }

    @Override
    public List<String> getAllElementTypes(NodeRef caseNodeRef) {
        List<NodeRef> configs = getElements(caseNodeRef, CaseConstants.ELEMENT_TYPES);
        return getConfigNames(configs);
    }

    @Override
    public List<NodeRef> getElements(NodeRef caseNodeRef, String configName) {
        exists(caseNodeRef);
        NodeRef configNode = needConfig(configName);
        CaseElementDAO strategy = getStrategy(configNode);
        return strategy.get(caseNodeRef, configNode);
    }

    @Override
    public List<NodeRef> getCases(NodeRef nodeRef, String configName) {
        exists(nodeRef);
        NodeRef configNode = needConfig(configName);
        CaseElementDAO strategy = getStrategy(configNode);
        return strategy.getCases(nodeRef, configNode);
    }

	@Override
	public void addElement(NodeRef nodeRef, NodeRef caseNodeRef,
			String configName) throws AlfrescoRuntimeException {
		exists(nodeRef);
		NodeRef configNode = needConfig(configName);
		CaseElementDAO strategy = getStrategy(configNode);
		strategy.add(nodeRef, caseNodeRef, configNode);
	}
	
    @Override
    public void addElements(Collection<NodeRef> elements, NodeRef caseNodeRef,
            String elementType) throws AlfrescoRuntimeException {
        exists(caseNodeRef);
        NodeRef configNode = needConfig(elementType);
        CaseElementDAO strategy = getStrategy(configNode);
        strategy.addAll(elements, caseNodeRef, configNode);
    }

	@Override
	public void removeElement(NodeRef nodeRef, NodeRef caseNodeRef, String configName)
			throws AlfrescoRuntimeException {
		exists(nodeRef);
		NodeRef configNode = needConfig(configName);
		CaseElementDAO strategy = getStrategy(configNode);
		strategy.remove(nodeRef, caseNodeRef, configNode);
	}

	@Override
	public NodeRef destination(NodeRef caseNodeRef, String configName)
			throws AlfrescoRuntimeException {

		NodeRef configNode = getConfig(configName);
		if (configNode == null)
			throw new AlfrescoRuntimeException("Can not find config node by name: " + configName);

		CaseElementDAO strategy = getStrategy(configNode);

		return strategy.destination(caseNodeRef, configNode);
	}
	
    @Override
    public void copyCaseToTemplate(NodeRef caseNodeRef, NodeRef templateRef) {
        exists(caseNodeRef);
        exists(templateRef);
        NodeRef elementTypesConfig = needConfig(CaseConstants.ELEMENT_TYPES);
        CaseElementDAO elementTypesStrategy = getStrategy(elementTypesConfig);
        List<NodeRef> elementConfigs = elementTypesStrategy.get(caseNodeRef, elementTypesConfig);
        
        for(NodeRef config : elementConfigs) {
            if(elementTypesConfig.equals(config))
                continue;
            NodeRef elementType = getOrCreateElementType(templateRef, config);
            
            if(shouldCopyElements(config)) {
                CaseElementDAO strategy = getStrategy(config);
                strategy.copyElementsToTemplate(caseNodeRef, elementType, config);
            }
        }
        
        adjustCopies();
    }

    @Override
    public void copyTemplateToCase(NodeRef templateRef, NodeRef caseNodeRef) {
        exists(caseNodeRef);
        exists(templateRef);
        NodeRef elementTypesConfig = needConfig(CaseConstants.ELEMENT_TYPES);
        CaseElementDAO elementTypesStrategy = getStrategy(elementTypesConfig);

        List<NodeRef> elementTypes = RepoUtils.getChildrenByAssoc(templateRef, ICaseTemplateModel.ASSOC_ELEMENT_TYPES, nodeService);
        for(NodeRef elementType : elementTypes) {
            NodeRef config = getTemplateElementConfig(elementType);
            elementTypesStrategy.add(config, caseNodeRef, elementTypesConfig);
            
            if(shouldCopyElements(config)) {
                CaseElementDAO strategy = getStrategy(config);
                strategy.copyElementsFromTemplate(elementType, caseNodeRef, config);
            }
        }
        
        adjustCopies();
    }

    private NodeRef getOrCreateElementType(NodeRef templateRef, NodeRef config) {
        String configName = getConfigName(config);
        NodeRef elementType = nodeService.getChildByName(templateRef, ICaseTemplateModel.ASSOC_ELEMENT_TYPES, configName);
        if(elementType == null) {
            elementType = nodeService.createNode(templateRef, 
                    ICaseTemplateModel.ASSOC_ELEMENT_TYPES, 
                    QName.createQName(ICaseTemplateModel.NAMESPACE, configName), 
                    ICaseTemplateModel.TYPE_ELEMENT_TYPE).getChildRef();
            nodeService.createAssociation(elementType, config, ICaseTemplateModel.ASSOC_ELEMENT_CONFIG);
        }
        return elementType;
    }

    private NodeRef getTemplateElementConfig(NodeRef elementType) {
        List<NodeRef> elementConfigs = RepoUtils.getTargetNodeRefs(elementType, ICaseTemplateModel.ASSOC_ELEMENT_CONFIG, nodeService);
        if(elementConfigs.size() == 0) {
            throw new IllegalStateException("Template element type does not contain element config reference: " + elementType);
        }
        return elementConfigs.get(0);
    }

    private boolean shouldCopyElements(NodeRef config) {
        Boolean copyElements = RepoUtils.getMandatoryProperty(config, ICaseModel.PROP_COPY_ELEMENTS, nodeService);
        return copyElements != null ? copyElements : false;
    }
    
    @SuppressWarnings("unchecked")
    private Map<NodeRef, NodeRef> getCopyMap() {
        return (Map<NodeRef,NodeRef>) AlfrescoTransactionSupport.getResource(KEY_COPIES);
    }
    
    /*package*/ void registerElementCopy(NodeRef original, NodeRef copy) {
        Map<NodeRef,NodeRef> copyMap = getCopyMap();
        if(copyMap == null) {
            copyMap = new HashMap<>();
            AlfrescoTransactionSupport.bindResource(KEY_COPIES, copyMap);
        }
        // the same original should not be copied twice
        NodeRef previousCopy = copyMap.put(original, copy);
        if(previousCopy != null) {
            throw new IllegalStateException("Each original element can only be copied once. Attemting to register second copy of: " + original);
        }
    }
    
    private void adjustCopies() {
        Map<NodeRef,NodeRef> copyMap = getCopyMap();
        if(copyMap == null) return;
        AlfrescoTransactionSupport.unbindResource(KEY_COPIES);
        
        // step 1: add children to copy map
        Queue<NodeRef> queue = new LinkedList<>();
        queue.addAll(copyMap.values());
        while(!queue.isEmpty()) {
            NodeRef node = queue.poll();
            List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(node);
            for(ChildAssociationRef childAssoc : childAssocs) {
                NodeRef childRef = childAssoc.getChildRef();
                NodeRef original = nodeService.getTargetAssocs(childRef, ContentModel.ASSOC_ORIGINAL).get(0).getTargetRef();
                copyMap.put(original, childRef);
            }
        }
        
        // step 2: adjust all links
        Collection<NodeRef> copies = copyMap.values();
        QNamePattern assocPattern = new ExceptQNamePattern(ContentModel.ASSOC_ORIGINAL);
        for(NodeRef copy : copies) {
            List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(copy, assocPattern);
            for(AssociationRef assoc : targetAssocs) {
                NodeRef targetOriginal = assoc.getTargetRef();
                NodeRef targetCopy = copyMap.get(targetOriginal);
                if(targetCopy == null) continue;
                QName assocType = assoc.getTypeQName();
                nodeService.removeAssociation(copy, targetOriginal, assocType);
                nodeService.createAssociation(copy, targetCopy, assocType);
            }
        }
    }
    
	protected void exists(NodeRef nodeRef) throws AlfrescoRuntimeException {
		if (!nodeService.exists(nodeRef))
			throw new AlfrescoRuntimeException("Specified node reference does not exist. nodeRef=" + nodeRef);
	}

    /*package*/ void invokeOnCaseElementAdd(NodeRef caseRef, NodeRef element, NodeRef config) {
        if(!nodeService.hasAspect(element, ICaseModel.ASPECT_ELEMENT)) {
            nodeService.addAspect(element, ICaseModel.ASPECT_ELEMENT, null);
        }
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementAddPolicy policy = onCaseElementAddDelegate.get(new HashSet<>(classes));
        policy.onCaseElementAdd(caseRef, element, config);
    }
    
    /*package*/ void invokeOnCaseElementUpdate(NodeRef caseRef, NodeRef element, NodeRef config) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementUpdatePolicy policy = onCaseElementUpdateDelegate.get(new HashSet<>(classes));
        policy.onCaseElementUpdate(caseRef, element, config);
    }
    
    /*package*/ void invokeOnCaseElementRemove(NodeRef caseRef, NodeRef element, NodeRef config) {
        List<QName> classes = DictionaryUtils.getNodeClassNames(caseRef, nodeService);
        CaseElementPolicies.OnCaseElementRemovePolicy policy = onCaseElementRemoveDelegate.get(new HashSet<>(classes));
        policy.onCaseElementRemove(caseRef, element, config);
    }
    
    public void onElementConfigCreated(ChildAssociationRef childAssoc) {
        final NodeRef configNode = childAssoc.getChildRef();
        if(!nodeService.exists(configNode)) {
            return;
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Initializing element config " + configNode + " of type " + nodeService.getType(configNode));
        }
        
        final CaseElementDAO strategy = getStrategy(configNode);
        if(strategy == null) {
            // assume, that this strategy is not yet registered
            // when it is registered, this method will be called again
            return;
        }
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() throws Exception {
                strategy.intializeBehaviours(configNode);
                return null;
            }
        });
        // TODO support unbinding behaviours
    }

    public void onElementUpdated(NodeRef element, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if(!nodeService.exists(element)) return;
        
        boolean elementHasCases = false;
        
        // check all existing case element configurations:
        List<NodeRef> configs = this.getAllElementConfigs();
        for(NodeRef config : configs) {
            CaseElementDAO strategy = getStrategy(config);
            List<NodeRef> caseNodes = strategy.getCases(element, config);
            if(caseNodes.size() > 0) elementHasCases = true;
            for(NodeRef caseNode : caseNodes) {
                invokeOnCaseElementUpdate(caseNode, element, config);
            }
        }
        // if there was no case, it is not an element anymore
        if(!elementHasCases) {
            nodeService.removeAspect(element, ICaseModel.ASPECT_ELEMENT);
        }
    }
    
    public void onElementDeleted(NodeRef element) {
        if(!nodeService.exists(element)) return;
        
        // check all existing case element configurations:
        List<NodeRef> configs = this.getAllElementConfigs();
        for(NodeRef config : configs) {
            CaseElementDAO strategy = getStrategy(config);
            List<NodeRef> caseNodes = strategy.getCases(element, config);
            for(NodeRef caseNode : caseNodes) {
                invokeOnCaseElementRemove(caseNode, element, config);
            }
        }
    }

    /*package*/ void register(CaseElementDAO strategy) {
        if(logger.isDebugEnabled()) {
            logger.debug("Registering strategy " + strategy);
        }
        QName configType = strategy.getElementConfigType();
        strategies.put(configType, strategy);
        if(initialized) {
            init(strategy);
        }
    }

    private void init(CaseElementDAO strategy) {
        QName configType = strategy.getElementConfigType();
        List<NodeRef> configs = getElementConfigsByType(configType);
        if(logger.isDebugEnabled()) {
            logger.debug("Initializing strategy " + strategy + ": Found " + configs.size() + " case element config nodes of this type");
        }
        for(NodeRef config : configs) {
            onElementConfigCreated(nodeService.getPrimaryParent(config));
        }
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event) {
        // initialize all previously registered strategies:
        this.initialized  = true;
        if(logger.isDebugEnabled()) {
            logger.debug("Initializing " + strategies.size() + " case element strategies");
        }
        for(CaseElementDAO strategy : strategies.values()) {
            init(strategy);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        // nothing to do
    }

}

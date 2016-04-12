package ru.citeck.ecos.behavior.tk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.citeck.ecos.journals.JournalService;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.FieldType;
import ru.citeck.ecos.search.SearchPredicate;
import ru.citeck.ecos.utils.RepoUtils;

public class SiteDocumentTypesBehaviour implements 
    NodeServicePolicies.OnCreateAssociationPolicy, 
    NodeServicePolicies.OnDeleteAssociationPolicy
{
    private static Log logger = LogFactory.getLog(SiteDocumentTypesBehaviour.class);
    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private SiteService siteService;
    private NamespaceService namespaceService;
    
    public void init() {
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
                SiteModel.TYPE_SITE, ClassificationModel.ASSOC_SITE_DOCUMENT_TYPES, 
                new JavaBehaviour(this, "onCreateAssociation"));
        policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, 
                SiteModel.TYPE_SITE, ClassificationModel.ASSOC_SITE_DOCUMENT_TYPES, 
                new JavaBehaviour(this, "onDeleteAssociation"));
    }

    @Override
    public void onCreateAssociation(AssociationRef assoc) {
        NodeRef site = assoc.getSourceRef();
        NodeRef type = assoc.getTargetRef();
        if(!nodeService.exists(site) || !nodeService.exists(type))
            return;
        
        String siteName = siteService.getSiteShortName(site);
        Serializable siteTitle = nodeService.getProperty(site, ContentModel.PROP_TITLE);
        if(siteTitle == null) siteTitle = siteName;
        
        String typeName = RepoUtils.getProperty(type, ContentModel.PROP_NAME, nodeService);
        Serializable typeTitle = nodeService.getProperty(type, ContentModel.PROP_TITLE);
        if(typeTitle == null) typeTitle = typeName;
        
        // create folder in document library
        NodeRef doclib = RepoUtils.getOrCreateSiteContainer(siteName, SiteService.DOCUMENT_LIBRARY, siteService);
        
        List<NodeRef> containers = RepoUtils.getChildrenByProperty(doclib, 
                ClassificationModel.PROP_RELATES_TO_TYPE, type, nodeService);
        NodeRef documentsFolder;
        if(containers.isEmpty()) {
            Map<QName, Serializable> containerProps = new HashMap<>();
            containerProps.put(ClassificationModel.PROP_RELATES_TO_TYPE, type);
            containerProps.put(ContentModel.PROP_NAME, typeName);
            containerProps.put(ContentModel.PROP_TITLE, typeTitle);
            documentsFolder = nodeService.createNode(doclib, ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(ClassificationModel.CLASSIFICATION_TYPE_KIND_NAMESPACE, type.getId()), 
                    ContentModel.TYPE_FOLDER, containerProps).getChildRef();
        } else {
            documentsFolder = containers.get(0);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Container on site '" + siteName + "' for type '" + typeName + 
                    (containers.isEmpty() ? "' created: " : "' found: ") + documentsFolder);
        }

        // journals container
        NodeRef journalsContainer = RepoUtils.getOrCreateSiteContainer(siteName, JournalService.JOURNALS_CONTAINER, siteService);
        
        // journals list
        String journalsListName = "site-" + siteName + "-main";
        NodeRef journalsList = RepoUtils.getChildByName(journalsContainer, ContentModel.ASSOC_CONTAINS, journalsListName, nodeService);
        if(journalsList == null) {

            Map<QName, Serializable> journalsListProps = new HashMap<>();
            journalsListProps.put(ContentModel.PROP_NAME, journalsListName);
            journalsListProps.put(ContentModel.PROP_TITLE, siteTitle);
            
            journalsList = nodeService.createNode(journalsContainer, ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(JournalsModel.JOURNAL_NAMESPACE, journalsListName), 
                    JournalsModel.TYPE_JOURNALS_LIST, 
                    journalsListProps).getChildRef();

            if(logger.isDebugEnabled()) {
                logger.debug("Journals list on site " + siteName + " created: " + journalsList);
            }

        } else if(!JournalsModel.TYPE_JOURNALS_LIST.equals(nodeService.getType(journalsList))) {
            throw new RuntimeException("Name '" + journalsListName + "' is reserved not by journals list");
        } else if(logger.isDebugEnabled()) {
            logger.debug("Journals list on site " + siteName + " found: " + journalsList);
        }
        
        // journal
        List<NodeRef> journals = RepoUtils.getChildrenByProperty(journalsContainer, 
                ClassificationModel.PROP_RELATES_TO_TYPE, type, nodeService);
        NodeRef journal;
        if(journals.isEmpty()) {
            
            String journalType = RepoUtils.getProperty(type, ClassificationModel.PROP_JOURNAL_TYPE, nodeService);
            if(journalType == null) journalType = "ecos-documents";
            
            Map<QName, Serializable> journalProps = new HashMap<>();
            journalProps.put(ClassificationModel.PROP_RELATES_TO_TYPE, type);
            journalProps.put(ContentModel.PROP_TITLE, typeTitle);
            journalProps.put(JournalsModel.PROP_JOURNAL_TYPE, journalType);
            
            journal = nodeService.createNode(journalsContainer, ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(JournalsModel.JOURNAL_NAMESPACE, type.getId()), 
                    JournalsModel.TYPE_JOURNAL, journalProps).getChildRef();
            
            // create criteria
            QName alfrescoType = RepoUtils.getProperty(type, ClassificationModel.PROP_APPLIED_TYPE, nodeService);
            if(alfrescoType == null)
                alfrescoType = QName.createQName("{http://www.citeck.ru/model/content/ecos/1.0}document");
            String path = nodeService.getPath(site).toPrefixString(namespaceService);
            
            nodeService.createNode(journal, JournalsModel.ASSOC_SEARCH_CRITERIA, 
                    QName.createQName(JournalsModel.JOURNAL_NAMESPACE, "type"), JournalsModel.TYPE_CRITERION, 
                    criterionProps(FieldType.TYPE, SearchPredicate.TYPE_EQUALS, alfrescoType));
            nodeService.createNode(journal, JournalsModel.ASSOC_SEARCH_CRITERIA, 
                    QName.createQName(JournalsModel.JOURNAL_NAMESPACE, "site"), JournalsModel.TYPE_CRITERION, 
                    criterionProps(FieldType.PATH, SearchPredicate.PATH_DESCENDANT, path));
            
            Map<QName, Serializable> createVariantProps = new HashMap<>();
            createVariantProps.put(ContentModel.PROP_TITLE, typeTitle);
            createVariantProps.put(JournalsModel.PROP_TYPE, alfrescoType);
            createVariantProps.put(JournalsModel.PROP_FORM_ID, "");
            createVariantProps.put(JournalsModel.PROP_IS_DEFAULT, true);
            
            // create variant
            NodeRef createVariant = nodeService.createNode(journal, JournalsModel.ASSOC_CREATE_VARIANTS, 
                    QName.createQName(JournalsModel.JOURNAL_NAMESPACE, "default"), JournalsModel.TYPE_CREATE_VARIANT,
                    createVariantProps).getChildRef();
            
            // bind it all together
            nodeService.createAssociation(journalsList, journal, JournalsModel.ASSOC_JOURNALS);
            nodeService.createAssociation(createVariant, documentsFolder, JournalsModel.ASSOC_DESTINATION);
            
        } else {
            journal = journals.get(0);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Journal on site '" + siteName + "' for type '" + typeName + 
                    (journals.isEmpty() ? "' created: " : "' found: ") + journal);
        }
        
    }

    private Map<QName, Serializable> criterionProps(FieldType type, SearchPredicate predicate, Serializable value) {
        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(JournalsModel.PROP_FIELD_QNAME, QName.createQName(null, type.getValue()));
        props.put(JournalsModel.PROP_PREDICATE, predicate.getValue());
        props.put(JournalsModel.PROP_CRITERION_VALUE, value);
        return props;
    }

    @Override
    public void onDeleteAssociation(AssociationRef assoc) {
        NodeRef site = assoc.getSourceRef();
        NodeRef type = assoc.getTargetRef();
        if(!nodeService.exists(site) || !nodeService.exists(type))
            return;
        
        String siteName = siteService.getSiteShortName(site);
        
        // journals container
        NodeRef journalsContainer = siteService.getContainer(siteName, JournalService.JOURNALS_CONTAINER);
        if(journalsContainer == null) return;
        
        // journal
        List<NodeRef> journals = RepoUtils.getChildrenByProperty(journalsContainer, 
                ClassificationModel.PROP_RELATES_TO_TYPE, type, nodeService);
        for(NodeRef journal : journals) {
            RepoUtils.deleteNode(journal, nodeService);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }
    
}

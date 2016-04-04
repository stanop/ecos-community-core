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
package ru.citeck.ecos.behavior.common;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.model.SiteModel;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
//import javax.xml.namespace.QName;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.error.AlfrescoRuntimeException;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddJournalBehaviour implements NodeServicePolicies.OnCreateNodePolicy{

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String typeQname;
    private String typeQnameShort;
    private String typeNamePlural;// name creating journal
    private String typeTitlePlural;
    private String typeTitleSingular;
    private Boolean checkTypeSite; // do or not check type of site

    public void init() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef nodeRef = childAssocRef.getChildRef();
        String typeSite = (String)nodeService.getProperty(nodeRef, SiteModel.PROP_SITE_PRESET);

        if(checkTypeSite){
            if("file-site-dashboard".equals(typeSite)){
                doWork(nodeRef);
            }
        }else{
            doWork(nodeRef);
        }
    }

    private void doWork(NodeRef nodeRef){

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        String nameSite = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String titleSite = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);

        NodeRef journalsListNodeRef = createJournalsList(nodeRef, nameSite, titleSite);
        NodeRef journalNodeRef = createJournal(nodeRef, nameSite, titleSite);

        nodeService.createAssociation(journalsListNodeRef, journalNodeRef, JournalsModel.ASSOC_JOURNALS);

    }

    private NodeRef createJournalsList(NodeRef nodeRef, String nameSite, String titleName) {

        String nameJournalList = "site-" + nameSite + "-main";

        //create journalsList
            ChildAssociationRef journalsList = nodeService.createNode(
                new NodeRef("workspace://SpacesStore/journal-meta-f-lists"), // parentNodeRef
                ContentModel.ASSOC_CONTAINS, // type of association
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nameJournalList), // name of association : "cm:site-{name-site}-main"
                JournalsModel.TYPE_JOURNALS_LIST // node type
        );
        // set properties
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, nameJournalList);                      //cm:name
                properties.put(ContentModel.PROP_TITLE, "Журналы сайта " + titleName);         //cm:title
            nodeService.setProperties(journalsList.getChildRef(), properties);

       return journalsList.getChildRef();
    }

    private NodeRef createJournal(NodeRef nodeRef, String nameSite, String titleName){

        //create journal
            ChildAssociationRef journal = nodeService.createNode(
                new NodeRef("workspace://SpacesStore/journal-meta-f-journals"), // parentNodeRef
                ContentModel.ASSOC_CONTAINS, // type of association
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, typeNamePlural), // example for journal : "cm:${journal.name}"
                JournalsModel.TYPE_JOURNAL // node type
        );

        // set properties of journal
            Map<QName, Serializable> propertiesJournal = new HashMap<QName, Serializable>();
                propertiesJournal.put(ContentModel.PROP_NAME, typeNamePlural + "-on-site-" + nameSite);                 //cm:name
                propertiesJournal.put(ContentModel.PROP_TITLE, typeTitlePlural + " для " + titleName);                   //cm:title
                propertiesJournal.put(JournalsModel.PROP_JOURNAL_TYPE, typeNamePlural);                                 //journal:journalType

        nodeService.setProperties(journal.getChildRef(), propertiesJournal);

        NodeRef journalNodeRef = journal.getChildRef();

        //create criterion for journal
            ChildAssociationRef criterion1 = nodeService.createNode(
                journalNodeRef, // parentNodeRef
                JournalsModel.ASSOC_SEARCH_CRITERIA, // type of association
                QName.createQName(JournalsModel.JOURNAL_NAMESPACE, "type"), // "journal:type"
                JournalsModel.TYPE_CRITERION // node type
        );
            NodeRef criterionNodeRef = criterion1.getChildRef();

        // set properties of criterion
            Map<QName, Serializable> propertiesCriterion = new HashMap<QName, Serializable>();
                propertiesCriterion.put(ContentModel.PROP_NAME, "type");                    // cm:name
                //propertiesCriterion.put(ContentModel.PROP_TITLE, "type");                   // cm:title
                propertiesCriterion.put(JournalsModel.PROP_PREDICATE, "type-equals");       // journal:predicate
                propertiesCriterion.put(JournalsModel.PROP_CRITERION_VALUE, typeQname);       // journal:predicate
                propertiesCriterion.put(JournalsModel.PROP_FIELD_QNAME, QName.createQName("", "type")); //journal:fieldQName {}type
            nodeService.setProperties(criterionNodeRef, propertiesCriterion);
            //nodeService.createAssociation(journalNodeRef, criterionNodeRef, JournalsModel.);

        //create journal:createVariant
            ChildAssociationRef createVariant1 = nodeService.createNode(
                journalNodeRef, // parentNodeRef
                JournalsModel.ASSOC_CREATE_VARIANTS, // type of association
                QName.createQName(JournalsModel.JOURNAL_NAMESPACE, "default"), // "journal:default"
                JournalsModel.TYPE_CREATE_VARIANT // node type
        );
            NodeRef createVariantNodeRef = createVariant1.getChildRef();

        // set properties of criterion
            Map<QName, Serializable> propertiesVariant = new HashMap<QName, Serializable>();
                propertiesVariant.put(ContentModel.PROP_NAME, "default");                    // cm:name
                propertiesVariant.put(ContentModel.PROP_TITLE, typeTitleSingular);                   // cm:title
                propertiesVariant.put(JournalsModel.PROP_TYPE, typeQnameShort);       // journal:type
                propertiesVariant.put(JournalsModel.PROP_FORM_ID, "");
                propertiesVariant.put(JournalsModel.PROP_IS_DEFAULT, false); // journal:isDefault
            nodeService.setProperties(createVariantNodeRef, propertiesVariant);

        // create folder
            ChildAssociationRef folder = nodeService.createNode(
                nodeRef, // parentNodeRef
                ContentModel.ASSOC_CONTAINS, // type of association
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, typeNamePlural), // ex. "cm:ecos-documents"
                ContentModel.TYPE_FOLDER// node type
        );

        NodeRef folderNodeRef = folder.getChildRef();

        // set properties of folder
            Map<QName, Serializable> propertiesFolder = new HashMap<QName, Serializable>();
                propertiesFolder.put(ContentModel.PROP_NAME, typeNamePlural);                    // cm:name
                propertiesFolder.put(ContentModel.PROP_TITLE, typeTitlePlural);                   // cm:title
            nodeService.setProperties(folderNodeRef, propertiesFolder);

        nodeService.createAssociation(createVariantNodeRef, folderNodeRef, JournalsModel.ASSOC_DESTINATION);

        return journalNodeRef;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setTypeNamePlural(String typeNamePlural) {
        this.typeNamePlural = typeNamePlural;
    }
    /*
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    */
    public void setCheckTypeSite(Boolean checkTypeSite) {
        this.checkTypeSite = checkTypeSite;
    }

    public void setTypeTitlePlural(String typeTitlePlural) {
        this.typeTitlePlural = typeTitlePlural;
    }
    public void setTypeTitleSingular(String typeTitleSingular) {
        this.typeTitleSingular = typeTitleSingular;
    }
    public void setTypeQname(String typeQname) {
        this.typeQname = typeQname;
    }
    public void setTypeQnameShort(String typeQnameShort) {
        this.typeQnameShort = typeQnameShort;
    }

}




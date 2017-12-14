package ru.citeck.ecos.cmmn.service;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.CmmnExportImportException;
import ru.citeck.ecos.cmmn.model.Case;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.content.config.dao.xml.XmlConfigDAO;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.LazyNodeRef;

import java.io.*;
import java.util.*;

import static ru.citeck.ecos.model.ICaseModel.PROP_CASE_ECOS_TYPE;
import static ru.citeck.ecos.model.ICaseModel.PROP_CASE_TYPE;

/**
 * Import-service for Cases in CMMN standard
 * 2016-06-16
 *
 * @author deathNC
 * @author Maxim Strizhov (maxim.strizhov@citeck.ru)
 * @author Pavel Simonov (pavel.simonov@citeck.ru)
 */
public class CaseImportService {

    private final Logger logger = LoggerFactory.getLogger(CaseImportService.class);

    private static final String DUPLICATE_COMMENT = "/*DUPLICATE*/";

    private NodeService nodeService;
    private LazyNodeRef caseTemplatesRoot;
    private ContentService contentService;

    @Autowired
    private CMMNUtils utils;
    @Autowired
    @Qualifier("caseTemplateConfigDAO")
    protected XmlConfigDAO<Definitions> configDAO;

    private Set<QName> templatesCompareProperties = new HashSet<>();

    public void init() {
        templatesCompareProperties.add(PROP_CASE_TYPE);
        templatesCompareProperties.add(PROP_CASE_ECOS_TYPE);
        templatesCompareProperties.add(ICaseModel.PROP_CASE_ECOS_KIND);
    }

    void importCase(byte[] xmlData) {
        InputStream inputStream = new ByteArrayInputStream(xmlData);
        importCase(inputStream);
    }

    public void importCase(InputStream inputStream) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, baos);
            byte[] bytes = baos.toByteArray();

            NodeRef templateRef = importCase(configDAO.read(bytes));

            ContentWriter writer;
            try {
                writer = contentService.getWriter(templateRef, ContentModel.PROP_CONTENT, true);
                writer.setEncoding("UTF-8");
                writer.setMimetype("text/xml");
                writer.putContent(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                logger.error("Failed to get writer", e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Failed to close file stream on form submit", e);
                }
            }

        } catch (Exception e) {
            logger.error("Cannot import case", e);
            throw new CmmnExportImportException("Cannot import case", e);
        }
    }

    private NodeRef importCase(Definitions definitions) throws CmmnExportImportException {

        Case caseItem = definitions.getCase().get(0);
        Map<QName, Serializable> contentProps = new HashMap<>();

        for (Map.Entry<javax.xml.namespace.QName, QName> mapping : CMMNUtils.CASE_ATTRIBUTES_MAPPING.entrySet()) {
            String value = caseItem.getOtherAttributes().get(mapping.getKey());
            Serializable convertedValue = utils.convertValueForRepo(mapping.getValue(), value);
            if (convertedValue != null) {
                contentProps.put(mapping.getValue(), convertedValue);
            }
        }

        NodeRef templateRef = findTemplate(contentProps);

        if (templateRef == null) {

            NodeRef templatesRoot = caseTemplatesRoot.getNodeRef();
            templateRef = nodeService.createNode(templatesRoot,
                                                 ContentModel.ASSOC_CONTAINS,
                                                 ICaseTemplateModel.TYPE_TEMPLATE,
                                                 ICaseTemplateModel.TYPE_TEMPLATE,
                                                 contentProps).getChildRef();
            logger.info("Create new template with templateRef = " + templateRef);

        } else {
            List<ChildAssociationRef> childAssociationRefs1 = nodeService.getChildAssocs(templateRef);
            for (ChildAssociationRef associationRef : childAssociationRefs1) {
                nodeService.deleteNode(associationRef.getChildRef());
            }
        }

        return templateRef;
    }

    private NodeRef findTemplate(Map<QName, Serializable> properties) {

        Set<NodeRef> templates = findTemplates(properties);
        NodeRef result = null;

        for (NodeRef templateRef : templates) {
            String condition = (String) nodeService.getProperty(templateRef, ICaseModel.PROP_CONDITION);
            if (condition == null || StringUtils.isBlank(condition) || condition.trim().equals("true")) {
                result = templateRef;
                break;
            }
        }

        for (NodeRef templateRef : templates) {
            if (result == null) {
                result = templateRef;
            } else if (!result.equals(templateRef)) {
                String condition = (String) nodeService.getProperty(templateRef, ICaseModel.PROP_CONDITION);
                if (condition == null) {
                    condition = "";
                }
                if (!condition.startsWith(DUPLICATE_COMMENT)) {
                    condition = DUPLICATE_COMMENT + condition;
                    nodeService.setProperty(templateRef, ICaseModel.PROP_CONDITION, condition);
                }
            }
        }
        return result;
    }

    private Set<NodeRef> findTemplates(Map<QName, Serializable> properties) {

        NodeRef templatesRoot = caseTemplatesRoot.getNodeRef();
        Set<NodeRef> result = new HashSet<>();

        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(templatesRoot);

        for (ChildAssociationRef associationRef : childAssociationRefs) {
            NodeRef child = associationRef.getChildRef();
            if (compareProperties(child, properties)) {
                result.add(child);
            }
        }

        return result;
    }

    private boolean compareProperties(NodeRef template, Map<QName, Serializable> properties) {

        for (QName property : templatesCompareProperties) {

            Object importedPropValue = properties.get(property);
            Object templatePropValue = nodeService.getProperty(template, property);

            if (!Objects.equals(importedPropValue, templatePropValue)) {
                return false;
            }
        }
        return true;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCaseTemplatesRoot(LazyNodeRef caseTemplatesRoot) {
        this.caseTemplatesRoot = caseTemplatesRoot;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}

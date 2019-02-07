package ru.citeck.ecos.eform.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.model.EFormModel;
import ru.citeck.ecos.model.EcosContentModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepoFormProvider extends AbstractFormProvider implements MutableFormProvider {

    private RepoContentDAO<EcosFormModel> formsContentDAO;
    private MetadataExtractor<EcosFormModel> metadataExtractor;
    private ContentService contentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public Optional<ContentData<EcosFormModel>> getContentData(NodeRef nodeRef) {
        return formsContentDAO.getContentData(nodeRef);
    }

    @Override
    public EcosFormModel getFormByKey(String formKey) {
        return getFormDataByKey(formKey).flatMap(ContentData::getData).orElse(null);
    }

    @Override
    public EcosFormModel getFormById(String id) {
        return getFormDataById(id).flatMap(ContentData::getData).orElse(null);
    }

    private Optional<ContentData<EcosFormModel>> getFormDataById(String formId) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EcosContentModel.PROP_ID, formId);
        return formsContentDAO.getFirstContentData(keys);
    }

    private Optional<ContentData<EcosFormModel>> getFormDataByKey(String formKey) {
        Map<QName, Serializable> keys = new HashMap<>();
        keys.put(EFormModel.PROP_FORM_KEY, formKey);
        return formsContentDAO.getFirstContentData(keys);
    }

    @Override
    public void save(EcosFormModel model) {

        ContentData<EcosFormModel> data = getFormDataById(model.getId())
                                                    .orElseThrow(() ->
                                                            new RuntimeException("Form not found " + model.getId()));
        saveContent(data.getNodeRef(), model);
    }

    private void saveContent(NodeRef formRef, EcosFormModel model) {
        ContentWriter writer = contentService.getWriter(formRef, ContentModel.PROP_CONTENT, true);
        try {
            writer.putContent(objectMapper.writeValueAsString(model));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error", e);
        }
    }

    @Override
    public void create(EcosFormModel model) {

        Map<QName, Serializable> metadata = metadataExtractor.getMetadata(model);
        NodeRef nodeRef = formsContentDAO.createNode(metadata);

        saveContent(nodeRef, model);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setMetadataExtractor(MetadataExtractor<EcosFormModel> metadataExtractor) {
        this.metadataExtractor = metadataExtractor;
    }

    public void setFormsContentDAO(RepoContentDAO<EcosFormModel> formsContentDAO) {
        this.formsContentDAO = formsContentDAO;
    }
}

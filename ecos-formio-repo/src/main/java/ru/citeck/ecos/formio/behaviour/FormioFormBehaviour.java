package ru.citeck.ecos.formio.behaviour;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.formio.FormioFormService;
import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.formio.repo.FormioFormMetadata;
import ru.citeck.ecos.model.EcosFormioModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FormioFormBehaviour extends AbstractBehaviour
                                 implements NodeServicePolicies.OnCreateNodePolicy,
                                            ContentServicePolicies.OnContentPropertyUpdatePolicy {

    private NodeService nodeService;
    private ContentService contentService;
    private FormioFormService formioFormService;
    private FormioFormMetadata metadataExtractor;
    private RepoContentDAO<FormioFormModel> formsContentDAO;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void beforeInit() {
        setClassName(EcosFormioModel.TYPE_FORM);
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
    }

    @PolicyMethod(policy = ContentServicePolicies.OnContentPropertyUpdatePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onContentPropertyUpdate(NodeRef nodeRef,
                                        QName propertyQName,
                                        org.alfresco.service.cmr.repository.ContentData beforeValue,
                                        org.alfresco.service.cmr.repository.ContentData afterValue) {

        if (ContentModel.PROP_CONTENT.equals(propertyQName)) {

            Optional<ContentData<FormioFormModel>> data = formsContentDAO.getContentData(nodeRef);

            data.flatMap(ContentData::getData)
                .ifPresent(model -> nodeService.addProperties(nodeRef, metadataExtractor.getMetadata(model)));
        }
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef formRef = childAssocRef.getChildRef();
        Map<QName, Serializable> props = nodeService.getProperties(formRef);

        Serializable content = props.get(ContentModel.PROP_CONTENT);
        if (content != null) {
            return;
        }

        String formKey = (String) props.get(EcosFormioModel.PROP_FORM_KEY);

        if (StringUtils.isBlank(formKey)) {
            throw new IllegalStateException("Form key can't be null." + props);
        }

        Map<QName, Serializable> key = new HashMap<>();
        key.put(EcosFormioModel.PROP_FORM_KEY, formKey);
        List<ContentData<FormioFormModel>> contentData = formsContentDAO.getContentData(key);

        if (contentData.size() > 1) {

            String nodeRefs = contentData.stream()
                                         .map(ContentData::getNodeRef)
                                         .map(NodeRef::toString)
                                         .collect(Collectors.joining(", "));

            throw new IllegalStateException("Form with key " + formKey + " already exists: " + nodeRefs);

        } else if (contentData.size() == 1) {

            NodeRef nodeRef = contentData.get(0).getNodeRef();

            if (!formRef.equals(nodeRef)) {

                throw new IllegalStateException("Form with key " + formKey + " already exists: " + nodeRef);
            }
        }

        FormioFormModel model = new FormioFormModel();

        model.setFormKey(formKey);
        model.setId(formRef.getId());
        model.setDescription((String) props.get(ContentModel.PROP_DESCRIPTION));
        model.setTitle((String) props.get(ContentModel.PROP_TITLE));
        model.setCustomModule((String) props.get(EcosFormioModel.PROP_CUSTOM_MODULE));
        model.setDefinition(formioFormService.getDefault().getDefinition());

        String modelStr;
        try {
            modelStr = mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't convert model to string", e);
        }
        contentService.getWriter(formRef, ContentModel.PROP_CONTENT, true).putContent(modelStr);
    }

    @Autowired
    @Qualifier("ecos.formio.repoFormsDAO")
    public void setFormsContentDAO(RepoContentDAO<FormioFormModel> formsContentDAO) {
        this.formsContentDAO = formsContentDAO;
    }

    @Autowired
    public void setMetadataExtractor(FormioFormMetadata metadataExtractor) {
        this.metadataExtractor = metadataExtractor;
    }

    @Autowired
    public void setFormioFormService(FormioFormService formioFormService) {
        this.formioFormService = formioFormService;
    }
}

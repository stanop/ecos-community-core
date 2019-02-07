package ru.citeck.ecos.eform.behaviour;

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
import ru.citeck.ecos.eform.EcosFormService;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.eform.repo.EcosFormMetadata;
import ru.citeck.ecos.model.EFormModel;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class EcosFormBehaviour extends AbstractBehaviour
                               implements NodeServicePolicies.OnCreateNodePolicy,
                                          ContentServicePolicies.OnContentPropertyUpdatePolicy {

    private NodeService nodeService;
    private ContentService contentService;
    private EcosFormService eformFormService;
    private EcosFormMetadata metadataExtractor;
    private RepoContentDAO<EcosFormModel> formsContentDAO;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void beforeInit() {
        setClassName(EFormModel.TYPE_FORM);
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

            Optional<ContentData<EcosFormModel>> data = formsContentDAO.getContentData(nodeRef);

            data.flatMap(ContentData::getData)
                .ifPresent(model -> nodeService.addProperties(nodeRef, metadataExtractor.getMetadata(model)));

            formsContentDAO.clearCache();
        }
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef formRef = childAssocRef.getChildRef();
        Map<QName, Serializable> props = nodeService.getProperties(formRef);

        String formKey = (String) props.get(EFormModel.PROP_FORM_KEY);

        if (StringUtils.isBlank(formKey)) {
            return;
        }

        //TODO: setup default form definition
        /*Map<QName, Serializable> key = new HashMap<>();
        key.put(EFormModel.PROP_FORM_KEY, formKey);
        List<ContentData<EcosFormModel>> contentData = formsContentDAO.getContentData(key);

        EcosFormModel model = new EcosFormModel();

        model.setFormKey(formKey);
        model.setId(formRef.getId());
        model.setDescription((String) props.get(ContentModel.PROP_DESCRIPTION));
        model.setTitle((String) props.get(ContentModel.PROP_TITLE));
        model.setCustomModule((String) props.get(EFormModel.PROP_CUSTOM_MODULE));
        model.setDefinition(eformFormService.getDefault().getDefinition());

        String modelStr;
        try {
            modelStr = mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't convert model to string", e);
        }
        contentService.getWriter(formRef, ContentModel.PROP_CONTENT, true).putContent(modelStr);*/
    }

    @Autowired
    @Qualifier("ecos.eform.repoFormsDAO")
    public void setFormsContentDAO(RepoContentDAO<EcosFormModel> formsContentDAO) {
        this.formsContentDAO = formsContentDAO;
    }

    @Autowired
    public void setMetadataExtractor(EcosFormMetadata metadataExtractor) {
        this.metadataExtractor = metadataExtractor;
    }

    @Autowired
    public void setEcosFormService(EcosFormService eformFormService) {
        this.eformFormService = eformFormService;
    }
}

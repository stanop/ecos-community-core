package ru.citeck.ecos.flowable.bpm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.citeck.ecos.model.EcosBpmModel;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Map;

@Component
public class EcosBpmAppModelUtils {

    public final static String MODEL_TYPE_PROCESS = "bpm_process";

    private NodeService nodeService;
    private ContentService contentService;
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${ecos.applications.model.deploy.url}")
    private String ecosAppsUrl;

    public void deployProcessModel(NodeRef nodeRef) {

        ParameterCheck.mandatory("nodeRef", nodeRef);

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContent(data);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        EcosAppModel model = new EcosAppModel();
        model.setData(data.toByteArray());
        model.setKey((String) properties.get(EcosBpmModel.PROP_PROCESS_ID));
        model.setMimetype(MimetypeMap.MIMETYPE_XML);
        model.setType(MODEL_TYPE_PROCESS);
        model.setName((String) properties.get(ContentModel.PROP_TITLE));

        restTemplate.postForObject(ecosAppsUrl, model, Object.class);
    }

    @Autowired
    public void serServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
    }
}

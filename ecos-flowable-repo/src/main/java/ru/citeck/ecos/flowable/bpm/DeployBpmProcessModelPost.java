package ru.citeck.ecos.flowable.bpm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.content.MimetypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.flowable.FlowableWorkflowComponent;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DeployBpmProcessModelPost extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private FlowableWorkflowComponent workflowComponent;

    {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        EcosAppModel bpmModel = objectMapper.readValue(req.getContent().getContent(), EcosAppModel.class);

        if (!MimetypeMap.MIMETYPE_XML.equals(bpmModel.getMimetype())) {
            throw new IllegalStateException("Illegal format: " + bpmModel.getMimetype() + " model: " + bpmModel);
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(bpmModel.getData());
        workflowComponent.deployDefinition(stream, bpmModel.getMimetype(), bpmModel.getName() + ".bpmn20.xml");

        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setWorkflowComponent(FlowableWorkflowComponent workflowComponent) {
        this.workflowComponent = workflowComponent;
    }
}

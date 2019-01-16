package ru.citeck.ecos.flowable.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.app.domain.editor.Model;
import org.flowable.app.service.editor.ModelImageService;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Format;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.model.EcosBpmModel;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EcosBpmContentSyncBehaviour extends AbstractBehaviour
                                         implements NodeServicePolicies.OnCreateNodePolicy,
                                                    ContentServicePolicies.OnContentPropertyUpdatePolicy {

    private static final QName PROP_XML = ContentModel.PROP_CONTENT;
    private static final QName PROP_JSON = EcosBpmModel.PROP_JSON_MODEL;
    private static final QName PROP_THUMBNAIL = EcosBpmModel.PROP_THUMBNAIL;

    private static final String ENCODING = "UTF-8";
    private static final String THUMBNAIL_MIMETYPE = "image/png";

    private static final Log logger = LogFactory.getLog(EcosBpmContentSyncBehaviour.class);

    private ContentService contentService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ModelImageService modelImageService;

    @Override
    protected void beforeInit() {
        setClassName(EcosBpmModel.TYPE_PROCESS_MODEL);
        contentService = serviceRegistry.getContentService();
    }

    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef nodeRef = childAssocRef.getChildRef();

        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

        Serializable content = props.get(PROP_XML);

        if (content == null) {

            String procId = (String) props.get(EcosBpmModel.PROP_PROCESS_ID);
            String name = (String) props.get(ContentModel.PROP_NAME);

            BpmnModel bpmnModel = new BpmnModel();
            Process process = new Process();
            bpmnModel.addProcess(process);

            process.setId(procId);
            process.setName(name);

            StartEvent startEvent = new StartEvent();
            startEvent.setId("start");
            process.addFlowElement(startEvent);

            ContentWriter writer = contentService.getWriter(nodeRef, PROP_XML, true);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            writer.putContent(new ByteArrayInputStream(xmlConverter.convertToXML(bpmnModel, ENCODING)));

        } else {

            ContentReader reader = contentService.getReader(nodeRef, PROP_XML);
            if (reader == null || !reader.exists()) {
                return;
            }

            try (InputStream in = reader.getContentInputStream()) {

                BpmnModel model = readModelFromXml(in);

                List<Process> processes = model.getProcesses();
                if (processes.size() > 0) {

                    Process process = processes.get(0);

                    Map<QName, Serializable> nodeProps = new HashMap<>();
                    nodeProps.put(EcosBpmModel.PROP_PROCESS_ID, process.getId());
                    nodeProps.put(ContentModel.PROP_TITLE, process.getName());
                    nodeProps.put(ContentModel.PROP_DESCRIPTION, process.getDocumentation());

                    nodeService.addProperties(nodeRef, nodeProps);
                }

            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    @PolicyMethod(policy = ContentServicePolicies.OnContentPropertyUpdatePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onContentPropertyUpdate(NodeRef nodeRef,
                                        QName propertyQName,
                                        ContentData beforeValue,
                                        ContentData afterValue) {

        if (PROP_XML.equals(propertyQName)) {
            xmlToJson(nodeRef, afterValue);
        } else if (PROP_JSON.equals(propertyQName)) {
            jsonToXml(nodeRef, afterValue);
        }
    }

    private BpmnModel xmlToJson(NodeRef nodeRef, ContentData data) {

        return convert(nodeRef, data, Format.JSON.mimetype(), PROP_JSON, input -> {

            BpmnModel bpmnModel = readModelFromXml(input);

            if (bpmnModel.getLocationMap().size() == 0) {
                BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
                bpmnLayout.execute();
            }

            BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
            ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);

            generateThumbnail(nodeRef, modelNode);

            try {
                return new BpmnModelData(bpmnModel, objectMapper.writeValueAsBytes(modelNode));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not write JSON", e);
            }
        });
    }

    private BpmnModel readModelFromXml(InputStream input) {

        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader;

        try {
            xmlStreamReader = xmlFactory.createXMLStreamReader(input, ENCODING);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Could not read XML", e);
        }

        return xmlConverter.convertToBpmnModel(xmlStreamReader);
    }

    private BpmnModel jsonToXml(NodeRef nodeRef, ContentData data) {

        return convert(nodeRef, data, Format.XML.mimetype(), PROP_XML, input -> {

            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();

            JsonNode jsonModel;
            try {
                jsonModel = objectMapper.readTree(input);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read JSON", e);
            }
            generateThumbnail(nodeRef, jsonModel);

            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(jsonModel);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();

            return new BpmnModelData(bpmnModel, xmlConverter.convertToXML(bpmnModel, ENCODING));
        });
    }

    private void generateThumbnail(NodeRef nodeRef, JsonNode node) {

        Model model = new Model();
        model.setId(nodeRef.toString());

        if (node instanceof ObjectNode) {

            byte[] thumbnail = modelImageService.generateThumbnailImage(model, (ObjectNode) node);

            ContentWriter writer = contentService.getWriter(nodeRef, PROP_THUMBNAIL, true);
            writer.setEncoding(ENCODING);
            writer.setMimetype(THUMBNAIL_MIMETYPE);
            writer.putContent(new ByteArrayInputStream(thumbnail));

        } else {

            logger.error("Wrong type of node: " + (node != null ? node.getClass() : null));
        }
    }

    private BpmnModel convert(NodeRef nodeRef,
                              ContentData data,
                              String targetMimetype,
                              QName targetProp,
                              Function<InputStream, BpmnModelData> convert) {

        if (!nodeService.exists(nodeRef)) {
            return null;
        }

        ContentReader reader = contentService.getRawReader(data.getContentUrl());
        if (!reader.exists()) {
            return null;
        }

        try (InputStream input = reader.getContentInputStream()) {

            ContentWriter writer = contentService.getWriter(nodeRef, targetProp, true);
            writer.setEncoding(ENCODING);
            writer.setMimetype(targetMimetype);

            BpmnModelData bpmnModelData = convert.apply(input);
            writer.putContent(new ByteArrayInputStream(bpmnModelData.getBytes()));

            return bpmnModelData.getModel();

        } catch (IOException e) {
            throw new IllegalStateException("Could not convert", e);
        }
    }

    @Autowired
    public void setModelImageService(ModelImageService modelImageService) {
        this.modelImageService = modelImageService;
    }

    private static class BpmnModelData {

        @Getter private BpmnModel model;
        @Getter private byte[] bytes;

        BpmnModelData(BpmnModel model, byte[] bytes) {
            this.model = model;
            this.bytes = bytes;
        }
    }
}

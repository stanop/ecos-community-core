package ru.citeck.ecos.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.module.type.DataType;
import ru.citeck.ecos.apps.module.type.impl.workflow.WorkflowModule;
import ru.citeck.ecos.apps.queue.EcosAppQueue;
import ru.citeck.ecos.apps.queue.EcosAppQueues;
import ru.citeck.ecos.apps.queue.ModulePublishMsg;
import ru.citeck.ecos.apps.queue.ModulePublishResultMsg;
import ru.citeck.ecos.model.EcosBpmModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EcosModuleMQListener {

    private static final NodeRef ROOT = new NodeRef("workspace://SpacesStore/ecos-bpm-process-root");
    private static final NodeRef CATEGORY = new NodeRef("workspace://SpacesStore/cat-doc-kind-ecos-bpm-default");

    private ConnectionFactory connectionFactory;
    private String consumerTag;
    private Channel channel;

    private SearchService searchService;
    private RetryingTransactionHelper retryHelper;
    private ContentService contentService;
    private NodeService nodeService;

    @Autowired
    public EcosModuleMQListener(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.retryHelper = serviceRegistry.getRetryingTransactionHelper();
        this.contentService = serviceRegistry.getContentService();
    }

    @PostConstruct
    public void init() {
        if (connectionFactory == null) {
            log.debug("Connection factory is not initialized");
            return;
        }
        try {
            initImpl();
        } catch (Exception e) {
            log.error("MQ Connection failed", e);
        }
    }

    private void initImpl() throws Exception {

        channel = connectionFactory.createConnection().createChannel(true);

        EcosAppQueue queue = EcosAppQueues.createQueue(EcosAppQueues.PUBLISH_PREFIX, WorkflowModule.TYPE);

        channel.queueDeclare(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete(),
                null
        );
        consumerTag = channel.basicConsume(
                queue.getName(),
                false,
                this::handleMqMessage,
                this::handleConsumeCancel
        );

        log.info("Subscribe workflow consumer. Tag: " + consumerTag);
    }

    private void handleConsumeCancel(String consumerTag) {
        log.error("Handle consume cancel: " + consumerTag);
    }

    private void handleMqMessage(String consumerTag, Delivery message) {
        try {
            handleMqMessageImpl(message);
        } catch (Exception e) {
            log.error("Message handling failed", e);
            try {
                channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void handleMqMessageImpl(Delivery message) throws IOException {

        long deliveryTag = message.getEnvelope().getDeliveryTag();

        log.info("Message received");

        if (message.getBody() == null) {
            log.error("Message body is null");
            channel.basicAck(deliveryTag, false);
            return;
        }

        ModulePublishMsg publishMsg;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(message.getBody()));
            publishMsg = (ModulePublishMsg) ois.readObject();
        } catch (Exception e) {
            log.error("Module can't be published. Body: " + Base64.getEncoder().encodeToString(message.getBody()), e);
            channel.basicAck(deliveryTag, false);
            return;
        }

        try {

            if (publishMsg.getId() == null || publishMsg.getId().isEmpty()) {
                channel.basicAck(deliveryTag, false);
                throw new IllegalArgumentException("Incorrect message");
            }

            retryHelper.doInTransaction(() -> {
                AuthenticationUtil.runAsSystem(() -> {
                    handleMqPublishMsg(publishMsg);
                    return null;
                });
                return null;
            }, false, true);

            log.info("Process published: " + publishMsg.getId());

            channel.basicAck(deliveryTag, false);
            sendPublishResult(publishMsg, true, null);

        } catch (Exception e) {
            log.error("Message handling error", e);
            channel.basicAck(deliveryTag, false);
            sendPublishResult(publishMsg, false, e.getMessage());
        }
    }

    private void sendPublishResult(ModulePublishMsg publishMsg, boolean isSuccess, String msg) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);

        ModulePublishResultMsg result = new ModulePublishResultMsg();

        result.setRevId(publishMsg.getRevId());
        result.setSuccess(isSuccess);
        result.setMsg(msg);

        oos.writeObject(result);

        channel.basicPublish("", EcosAppQueues.PUBLISH_RESULT_ID, null, out.toByteArray());
    }

    private void handleMqPublishMsg(ModulePublishMsg publishMsg) {

        NodeRef processNode = FTSQuery.create()
                .type(EcosBpmModel.TYPE_PROCESS_MODEL).and()
                .exact(EcosBpmModel.PROP_PROCESS_ID, publishMsg.getId())
                .transactional()
                .queryOne(searchService)
                .orElse(null);

        if (processNode == null) {

            String localName = publishMsg.getId().replace("$", "_");
            QName assocQName = QName.createQNameWithValidLocalName(NamespaceService.SYSTEM_MODEL_1_0_URI, localName);

            Map<QName, Serializable> props = new HashMap<>();
            props.put(EcosBpmModel.PROP_PROCESS_ID, publishMsg.getId());
            props.put(EcosBpmModel.PROP_CATEGORY, CATEGORY);

            processNode = nodeService.createNode(
                    ROOT,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    EcosBpmModel.TYPE_PROCESS_MODEL,
                    props
            ).getChildRef();
        }

        QName prop;
        if (DataType.JSON.equals(publishMsg.getDataType())) {
            prop = EcosBpmModel.PROP_JSON_MODEL;
        } else {
            prop = ContentModel.PROP_CONTENT;
        }

        ContentWriter writer = contentService.getWriter(processNode, prop, true);
        writer.putContent(new ByteArrayInputStream(publishMsg.getData()));
    }



    @Autowired(required = false)
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void cancel() {
        if (consumerTag != null) {
            try {
                channel.basicCancel(consumerTag);
            } catch (IOException e) {
                log.error("Error", e);
                throw new RuntimeException(e);
            }
        }
    }
}

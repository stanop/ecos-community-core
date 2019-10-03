package ru.citeck.ecos.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.module.type.impl.workflow.WorkflowModule;
import ru.citeck.ecos.apps.queue.EcosAppQueue;
import ru.citeck.ecos.apps.queue.EcosAppQueues;
import ru.citeck.ecos.apps.queue.ModulePublishMsg;
import ru.citeck.ecos.apps.queue.ModulePublishResultMsg;
import ru.citeck.ecos.records2.utils.MandatoryParam;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@DependsOn({"moduleStarter"})
public class EcosModuleMQListener extends AbstractLifecycleBean {

    private ConnectionFactory connectionFactory;
    private RabbitTemplate rabbitTemplate;

    private String consumerTag;
    private Channel channel;

    private RetryingTransactionHelper retryHelper;

    private Map<String, EcosModulePublisher> publishers = new HashMap<>();

    @Autowired
    public EcosModuleMQListener(ServiceRegistry serviceRegistry, List<EcosModulePublisher> publishers) {
        this.retryHelper = serviceRegistry.getRetryingTransactionHelper();
        publishers.forEach(p -> this.publishers.put(p.getModuleType(), p));
    }

    @Override
    protected void onBootstrap(ApplicationEvent event) {

        log.info("Initialize EcosModuleMQListener");

        if (connectionFactory == null || rabbitTemplate == null) {
            log.debug("RabbitMQ is not initialized");
            return;
        }
        try {
            initImpl();
        } catch (Exception e) {
            log.error("MQ Connection failed", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {
        cancel();
    }

    private void declareQueue(EcosAppQueue queue) throws IOException {
        channel.queueDeclare(
                queue.getName(),
                queue.isDurable(),
                queue.isExclusive(),
                queue.isAutoDelete(),
                null
        );
    }

    private void initImpl() throws Exception {

        channel = connectionFactory.createConnection().createChannel(true);

        EcosAppQueue queue = EcosAppQueues.getQueueForType(WorkflowModule.TYPE);
        declareQueue(queue);
        declareQueue(EcosAppQueues.PUBLISH_ERROR);

        consumerTag = channel.basicConsume(
                queue.getName(),
                true,
                this::handleMqMessage,
                this::handleConsumeCancel
        );

        log.info("Subscribe EcosModule consumer. Tag: " + consumerTag);
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
                rabbitTemplate.convertAndSend(EcosAppQueues.PUBLISH_ERROR_ID, message);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void handleMqMessageImpl(Delivery message) throws Exception {

        log.info("Message received");

        if (message.getBody() == null) {
            throw new IllegalArgumentException("Message body is null");
        }

        ModulePublishMsg publishMsg;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(message.getBody()));
            publishMsg = (ModulePublishMsg) ois.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Module publish message read error", e);
        }

        log.info("Start module publishing: " + publishMsg.getId() + " (" + publishMsg.getType() + ")");

        try {

            MandatoryParam.checkString("id", publishMsg.getId());
            MandatoryParam.checkString("type", publishMsg.getType());

            EcosModulePublisher publisher = publishers.get(publishMsg.getType());
            if (publisher == null) {
                throw new IllegalArgumentException("Publisher is not registered for type " + publishMsg.getType());
            }

            retryHelper.doInTransaction(() -> {
                AuthenticationUtil.runAsSystem(() -> {
                    publisher.publish(publishMsg);
                    return null;
                });
                return null;
            }, false, true);

            log.info("Module published: " + publishMsg.getId() + " (" + publishMsg.getType() + ")");

            sendPublishResult(publishMsg, true, null);

        } catch (Exception e) {
            try {
                sendPublishResult(publishMsg, false, e.getMessage());
            } catch (Exception resultEx) {
                log.error("Publish result sending failed", resultEx);
            }
            throw e;
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

        rabbitTemplate.convertAndSend(EcosAppQueues.PUBLISH_RESULT_ID, result);
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

    @Autowired(required = false)
    @Qualifier("historyRabbitConnectionFactory")
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Autowired(required = false)
    @Qualifier("historyRabbitTemplate")
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}

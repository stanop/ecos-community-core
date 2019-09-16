package ru.citeck.ecos.rabbit.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.queue.EcosAppQueues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RabbitMQPackageSender implements ApplicationListener<ContextRefreshedEvent> {

    private static final String PACKAGE_FORMS_LOCATION = "package.forms.location";
    private static Logger LOGGER = LoggerFactory.getLogger(RabbitMQPackageSender.class);

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    private Environment env;

    @Autowired
    public RabbitMQPackageSender(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                                 @Qualifier("retryTemplate") RetryTemplate retryTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        String filepath = env.getProperty(PACKAGE_FORMS_LOCATION);
        Path path = Paths.get(filepath);
        try {
            byte[] fileBytes = Files.readAllBytes(path);
            LOGGER.info("Sending package to RabbitMQ: " + path.getFileName());
            retryTemplate.execute(
                    task -> rabbitTemplate.convertSendAndReceive(EcosAppQueues.ECOS_APPS_UPLOAD_ID, fileBytes),
                    callback -> {
                        LOGGER.error("Package isn't send to RabbitMQ: " + path.getFileName());
                        return null;
                    });
        } catch (IOException ioe) {
            LOGGER.error(ioe.getLocalizedMessage());
        }
    }
}

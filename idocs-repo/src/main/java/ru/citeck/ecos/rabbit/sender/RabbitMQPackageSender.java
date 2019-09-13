package ru.citeck.ecos.rabbit.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.queue.EcosAppQueues;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RabbitMQPackageSender {

    private static final String PACKAGE_FORMS_LOCATION = "package.forms.location";
    private static Logger LOGGER = LoggerFactory.getLogger(RabbitMQPackageSender.class);

    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    public RabbitMQPackageSender(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        sendFile();
    }

    private void sendFile() {
        String filepath = env.getProperty(PACKAGE_FORMS_LOCATION);
        try {
            Path path = Paths.get(filepath);
            byte[] fileBytes = Files.readAllBytes(path);
            LOGGER.info("Sending package to RabbitMQ");
            rabbitTemplate.convertAndSend(EcosAppQueues.ECOS_APPS_UPLOAD_ID, fileBytes);
        } catch (IOException ioe) {
            LOGGER.error(ioe.getLocalizedMessage());
        }
    }
}

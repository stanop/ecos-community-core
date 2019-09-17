package ru.citeck.ecos.rabbit.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.queue.EcosAppQueues;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class RabbitMQZipPackagesSender implements ApplicationListener<ContextRefreshedEvent> {

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;
    private List<String> locations;

    @Autowired
    public RabbitMQZipPackagesSender(@Qualifier("historyRabbitTemplate") RabbitTemplate rabbitTemplate,
                                     @Qualifier("retryTemplate") RetryTemplate retryTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryTemplate = retryTemplate;
    }

    private void sendFiles(List<String> locations) {
        ExecutorService executor = Executors.newCachedThreadPool();
        locations.forEach(location ->
            executor.execute(() -> {
                try {
                    Resource resource = location.contains(":") ? new UrlResource(location) : new ClassPathResource(location);
                    Path path = Paths.get(resource.getURL().getPath());
                    byte[] fileBytes = Files.readAllBytes(path);
                    log.info("Sending package to RabbitMQ: " + path.getFileName());
                    retryTemplate.execute(
                            task -> rabbitTemplate.convertSendAndReceive(EcosAppQueues.ECOS_APPS_UPLOAD_ID, fileBytes),
                            callback -> {
                                log.error("Package isn't send to RabbitMQ: " + path.getFileName());
                                return null;
                            });
                } catch (IOException ioe) {
                    log.error("Exception when work with file", ioe);
                }
            })
        );
        executor.shutdown();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (rabbitTemplate != null) {
            sendFiles(locations);
        } else {
            log.warn("Bean \"historyRabbitTemplate\" wasn't initialized, packages not send");
        }
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}

package ru.citeck.ecos.rabbit.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.queue.EcosAppQueues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class RabbitMQPackageSender implements ApplicationListener<ContextRefreshedEvent> {

    private RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;

    @Autowired
    public RabbitMQPackageSender(@Qualifier("historyRabbitTemplate") RabbitTemplate rabbitTemplate,
                                 @Qualifier("retryTemplate") RetryTemplate retryTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.retryTemplate = retryTemplate;
    }

    private Set<Path> getZipPackagesPaths() {
        String projectPath = System.getProperty("user.dir");
        try (Stream<Path> paths = Files.list(Paths.get(projectPath))) {
            return paths
                    .filter(path -> Files.isDirectory(path) &&
                            path.getFileName().toString().endsWith("-repo"))
                    .map(path -> Paths.get(
                            path.toString(),
                            "target",
                            "classes",
                            "alfresco",
                            "module",
                            path.getFileName().toString(),
                            "ecos-apps"))
                    .collect(HashSet::new,
                            (stored, path) -> {
                                try {
                                    stored.addAll(Files.list(path)
                                            .filter(p -> p.getFileName().toString().endsWith(".zip"))
                                            .collect(Collectors.toSet()));
                                } catch (Exception ignored) {
                                }
                            },
                            HashSet::addAll);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendFiles(Set<Path> paths) {
        paths.forEach(path -> {
            try {
                byte[] fileBytes = Files.readAllBytes(path);
                log.info("Sending package to RabbitMQ: " + path.getFileName());
                retryTemplate.execute(
                        task -> rabbitTemplate.convertSendAndReceive(EcosAppQueues.ECOS_APPS_UPLOAD_ID, fileBytes),
                        callback -> {
                            log.error("Package isn't send to RabbitMQ: " + path.getFileName());
                            return null;
                        });
            } catch (IOException ioe) {
                log.error(ioe.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (rabbitTemplate != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Set<Path> paths = getZipPackagesPaths();
                if (paths != null) {
                    sendFiles(paths);
                }
            });
        } else {
            log.warn("Bean \"historyRabbitTemplate\" wasn't initialized, package not send");
        }
    }
}

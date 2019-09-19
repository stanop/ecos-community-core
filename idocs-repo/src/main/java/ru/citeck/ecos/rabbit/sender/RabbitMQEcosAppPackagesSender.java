package ru.citeck.ecos.rabbit.sender;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.queue.EcosAppQueues;
import ru.citeck.ecos.utils.ResourceResolver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class RabbitMQEcosAppPackagesSender implements ApplicationListener<ContextRefreshedEvent> {

    private final RabbitTemplate rabbitTemplate;
    private RetryTemplate retryTemplate;
    private List<String> locations;

    @Autowired
    public RabbitMQEcosAppPackagesSender(@Qualifier("historyRabbitTemplate") RabbitTemplate rabbitTemplate,
                                         @Qualifier("resourceResolver") ResourceResolver resolver) {
        this.rabbitTemplate = rabbitTemplate;
        try {
            locations = resolver.getResources(new String[] {"classpath*:alfresco/module/*/ecos-apps/**/*.zip"});
        } catch (IOException e) {
            log.error("Failed to get resources", e);
        }
    }

    /*
     * Resending packages manually.
     * Method added for debug.
     */
    public void resendPackages() {
        onApplicationEvent(null);
    }

    private void sendFiles(List<String> locations) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (String location : locations) {
                try {
                    Resource resource = location.contains(":") ? new UrlResource(location) :
                            new ClassPathResource(location);
                    byte[] fileBytes = IOUtils.toByteArray(resource.getInputStream());
                    log.info("Sending package to RabbitMQ: " + resource.getFilename());
                    RetryTemplate retryTemplate = getRetryTemplate();
                    Object callback = retryTemplate.execute(
                            t -> rabbitTemplate.convertSendAndReceive(EcosAppQueues.ECOS_APPS_UPLOAD_ID, fileBytes),
                            c -> {
                                log.error("Package isn't send to RabbitMQ: " + resource.getFilename(),
                                        c.getLastThrowable());
                                return c.getLastThrowable();
                            });
                    if (callback != null) {
                        break;
                    }

                } catch (IOException ioe) {
                    log.error("Exception when work with file", ioe);
                    break;
                }
            }
        });
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

    private RetryTemplate getRetryTemplate() {

        if (retryTemplate == null) {
            retryTemplate = new RetryTemplate();

            FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(10000);
            retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

            SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicy.setMaxAttempts(6);
            retryTemplate.setRetryPolicy(retryPolicy);
        }

        return retryTemplate;
    }
}

package ru.citeck.ecos.commands;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.rabbit.RabbitCommandsService;
import ru.citeck.ecos.commands.remote.RemoteCommandsService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import ru.citeck.ecos.commands.transaction.TransactionManager;
import ru.citeck.ecos.eureka.EurekaAlfInstanceConfig;

import java.util.concurrent.Callable;

@Slf4j
@Configuration
@DependsOn({"moduleStarter"})
public class CommandsServiceFactoryConfig extends CommandsServiceFactory {

    private ConnectionFactory connectionFactory;
    private RetryingTransactionHelper retryHelper;

    @Autowired
    private EurekaAlfInstanceConfig instanceConfig;

    @Bean
    @Override
    public CommandsService createCommandsService() {
        return super.createCommandsService();
    }

    @Bean
    @Override
    public CommandsProperties createProperties() {
        CommandsProperties props = new CommandsProperties();
        props.setAppInstanceId(instanceConfig.getInstanceId());
        props.setAppName(instanceConfig.getAppname());
        return props;
    }

    @Bean
    @Override
    public RemoteCommandsService createRemoteCommandsService() {

        if (connectionFactory != null) {
            try {
                Connection connection = connectionFactory.createConnection();
                Channel channel = connection.createChannel(false);
                return new RabbitCommandsService(this, channel);
            } catch (Exception e) {
                log.error("Cannot configure connection to RabbitMQ", e);
            }
        }
        log.warn("Rabbit mq host is null. Remote commands will not be available");
        return super.createRemoteCommandsService();
    }

    @NotNull
    @Override
    protected TransactionManager createTransactionManager() {
        return new TransactionManager() {
            @Override
            public <T> T doInTransaction(@NotNull Callable<T> callable) {
                return retryHelper.doInTransaction(() ->
                    AuthenticationUtil.runAsSystem(callable::call), false, false);
            }
        };
    }

    @Autowired(required = false)
    @Qualifier("historyRabbitConnectionFactory")
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        retryHelper = serviceRegistry.getRetryingTransactionHelper();
    }

    @Component
    public static class RemoteInitializer extends AbstractLifecycleBean {

        @Autowired
        private CommandsServiceFactoryConfig config;

        @Override
        protected void onBootstrap(ApplicationEvent event) {

            log.info("==================== Initialize Commands Rabbit Service ====================");

            try {
                config.createRemoteCommandsService().init();
            } catch (Exception e) {
                log.error("Commands remote service initialization failed", e);
            }
        }

        @Override
        protected void onShutdown(ApplicationEvent applicationEvent) {
        }
    }
}

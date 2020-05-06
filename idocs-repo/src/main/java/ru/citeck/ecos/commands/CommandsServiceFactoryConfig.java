package ru.citeck.ecos.commands;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.context.CommandCtxController;
import ru.citeck.ecos.commands.context.CommandCtxManager;
import ru.citeck.ecos.commands.rabbit.RabbitCommandsService;
import ru.citeck.ecos.commands.remote.RemoteCommandsService;
import ru.citeck.ecos.commands.transaction.TransactionManager;
import ru.citeck.ecos.eureka.EurekaAlfInstanceConfig;

import java.util.Properties;
import java.util.concurrent.Callable;

@Slf4j
@Configuration
@DependsOn({"moduleStarter"})
public class CommandsServiceFactoryConfig extends CommandsServiceFactory {

    private static final String RABBIT_MQ_HOST = "rabbitmq.server.host";
    private static final String RABBIT_MQ_PORT= "rabbitmq.server.port";
    private static final String RABBIT_MQ_USERNAME= "rabbitmq.server.username";
    private static final String RABBIT_MQ_PASSWORD = "rabbitmq.server.password";
    private static final String RABBIT_MQ_CHANNELS_COUNT = "commands.rabbitmq.channelsCount";

    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    private RetryingTransactionHelper retryHelper;

    @Autowired
    private EurekaAlfInstanceConfig instanceConfig;

    @Bean
    @Override
    public CommandsService createCommandsService() {
        return super.createCommandsService();
    }

    @Bean(name = "commandsProperties")
    @Override
    public CommandsProperties createProperties() {
        CommandsProperties props = new CommandsProperties();
        props.setAppInstanceId(instanceConfig.getInstanceId());
        props.setAppName(instanceConfig.getAppname());
        props.setRabbitChannelsCount(Integer.valueOf(properties.getProperty(RABBIT_MQ_CHANNELS_COUNT, "4")));
        return props;
    }

    @Bean
    @Override
    @DependsOn("commandsProperties")
    public RemoteCommandsService createRemoteCommandsService() {

        String host = properties.getProperty(RABBIT_MQ_HOST);
        if (StringUtils.isBlank(host)) {
            log.warn("Rabbit mq host is null. Remote commands won't be available");
            return super.createRemoteCommandsService();
        }

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setHost(host);
        connectionFactory.setPort(Integer.valueOf(properties.getProperty(RABBIT_MQ_PORT)));
        connectionFactory.setUsername(properties.getProperty(RABBIT_MQ_USERNAME));
        connectionFactory.setPassword(properties.getProperty(RABBIT_MQ_PASSWORD));

        return new RabbitCommandsService(this, connectionFactory);
    }

    @NotNull
    @Override
    protected TransactionManager createTransactionManager() {
        return new TransactionManager() {
            @Override
            public <T> T doInTransaction(@NotNull Callable<T> callable) {
                return retryHelper.doInTransaction(() -> {
                    CommandCtxManager commandCtxManager = getCommandCtxManager();
                    String currentUser = commandCtxManager.getCurrentUser();
                    return AuthenticationUtil.runAs(callable::call, currentUser);
                }, false, false);
            }
        };
    }

    @NotNull
    @Override
    protected CommandCtxController createCommandCtxController() {
        return new CommandCtxController() {
            @NotNull
            @Override
            public String setCurrentUser(@NotNull String username) {
                if (StringUtils.isEmpty(username)) {
                    username = AuthenticationUtil.getSystemUserName();
                }
                AuthenticationUtil.setRunAsUser(username);
                return AuthenticationUtil.getRunAsUser();
            }

            @NotNull
            @Override
            public String getCurrentUser() {
                String user = AuthenticationUtil.getRunAsUser();
                return user == null ? AuthenticationUtil.getSystemUserName() : user;
            }

            @NotNull
            @Override
            public String setCurrentTenant(@NotNull String tenant) {
                return tenant;
            }

            @NotNull
            @Override
            public String getCurrentTenant() {
                return "";
            }
        };
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

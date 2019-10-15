package ru.citeck.ecos.eapps;

import com.rabbitmq.client.Channel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.apps.EappsFactory;
import ru.citeck.ecos.apps.EcosAppsApiFactory;
import ru.citeck.ecos.apps.EcosAppsApiMock;
import ru.citeck.ecos.apps.app.EappTxnManager;
import ru.citeck.ecos.apps.app.io.AppModulesReader;
import ru.citeck.ecos.apps.app.io.EcosAppIO;
import ru.citeck.ecos.apps.rabbit.EappsRabbitApi;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class EcosAppsFactoryConfig extends EappsFactory {

    private Connection connection;
    private ConnectionFactory connectionFactory;
    private RetryingTransactionHelper retryHelper;

    @Override
    protected AppModulesReader createAppModulesReader() {

        AppModulesReader modulesReader = super.createAppModulesReader();
        Map<String, String> mapping = new HashMap<>();

        mapping.put("form", "ecos-forms");
        mapping.put("type", "ecos-types");
        mapping.put("section", "ecos-sections");

        modulesReader.setModuleLocations(mapping);
        return modulesReader;
    }

    @Bean
    @Override
    protected EcosAppIO createEcosAppIO() {
        EcosAppIO io = super.createEcosAppIO();
        io.getReader().setModulesRoot(null);
        return io;
    }

    @Bean
    @Override
    protected EcosAppsApiFactory createAppsApiFactory() {

        if (connectionFactory == null) {
            log.warn("Connection factory is null");
            return new EcosAppsApiMock();
        }

        Channel channel;
        try {
            connection = connectionFactory.createConnection();
            channel = connection.createChannel(false);
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    log.error("Connection close error", ex);
                }
            }
            log.error("Connection can't be established", e);
            return new EcosAppsApiMock();
        }

        return new EappsRabbitApi(this, channel);
    }

    @Override
    protected EappTxnManager createEappTxnManager() {
        return new EappTxnManager() {
            @Override
            public <R> R doInTransaction(Action<R> action) {
                return retryHelper.doInTransaction(() -> AuthenticationUtil.runAsSystem(action::run));
            }
        };
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        retryHelper = serviceRegistry.getRetryingTransactionHelper();
    }

    @Autowired(required = false)
    @Qualifier("historyRabbitConnectionFactory")
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}

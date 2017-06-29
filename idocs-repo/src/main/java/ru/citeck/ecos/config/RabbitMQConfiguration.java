package ru.citeck.ecos.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Rabbit MQ configuration
 */
@Configuration
public class RabbitMQConfiguration {

    /**
     * Properties constants
     */
    private static final String RABBIT_MQ_HOST = "rabbitmq.server.host";
    private static final String RABBIT_MQ_PORT= "rabbitmq.server.port";
    private static final String RABBIT_MQ_USERNAME= "rabbitmq.server.username";
    private static final String RABBIT_MQ_PASSWORD = "rabbitmq.server.password";

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;


    /**
     * Connection factory bean
     * @return Connection factory or null (in case of absence "rabbitmq.server.host" global property)
     */
    @Bean(name = "rabbitConnectionFactory")
    public CachingConnectionFactory cachingConnectionFactory() {
        if (properties.getProperty(RABBIT_MQ_HOST) != null) {
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.setHost(properties.getProperty(RABBIT_MQ_HOST));
            connectionFactory.setPort(Integer.valueOf(properties.getProperty(RABBIT_MQ_PORT)));
            connectionFactory.setUsername(properties.getProperty(RABBIT_MQ_USERNAME));
            connectionFactory.setPassword(properties.getProperty(RABBIT_MQ_PASSWORD));
            return connectionFactory;
        } else {
            return null;
        }
    }

    /**
     * Rabbit template bean
     * @param connectionFactory Connection factory
     * @return Rabbit template or null (in case of absence connection factory)
     */
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        if (connectionFactory != null) {
            return new RabbitTemplate(connectionFactory);
        } else {
            return null;
        }
    }
}

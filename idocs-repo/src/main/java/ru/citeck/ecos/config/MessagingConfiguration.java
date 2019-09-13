package ru.citeck.ecos.config;

import com.sun.media.jfxmedia.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jms.core.JmsTemplate;

import java.io.IOException;
import java.util.Properties;

/**
 * Messaging configuration
 */
@Configuration
@PropertySource("classpath:application.properties")
public class MessagingConfiguration {

    /**
     * Properties constants
     */
    private static final String RABBIT_MQ_HOST = "rabbitmq.server.host";
    private static final String RABBIT_MQ_PORT= "rabbitmq.server.port";
    private static final String RABBIT_MQ_USERNAME= "rabbitmq.server.username";
    private static final String RABBIT_MQ_PASSWORD = "rabbitmq.server.password";
    private static final String BROKER_URL = "messaging.broker.url";

    @Autowired
    Environment env;

    /**
     * ActiveMQ connection factory
     * @return ActiveMQ connection factory
     */
    @Bean(name = "activeMQConnectionFactory")
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(env.getProperty(BROKER_URL));
        return activeMQConnectionFactory;
    }

    /**
     * JMS template
     * @param factory Connection factory
     * @return JMS template
     */
    @Bean(name = "jmsTemplate")
    public JmsTemplate jmsTemplate(ActiveMQConnectionFactory factory) {
        if (factory != null) {
            return new JmsTemplate(factory);
        } else {
            return null;
        }
    }

    /**
     * Connection factory bean
     * @return Connection factory or null (in case of absence "rabbitmq.server.host" global property)
     */
    @Bean(name = "rabbitConnectionFactory")
    public CachingConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(env.getProperty(RABBIT_MQ_HOST));
        connectionFactory.setPort(Integer.valueOf(env.getProperty(RABBIT_MQ_PORT)));
        connectionFactory.setUsername(env.getProperty(RABBIT_MQ_USERNAME));
        connectionFactory.setPassword(env.getProperty(RABBIT_MQ_PASSWORD));
        return connectionFactory;
    }

    /**
     * Rabbit template bean
     * @param connectionFactory Connection factory
     * @return Rabbit template or null (in case of absence connection factory)
     */
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(@Qualifier("rabbitConnectionFactory") CachingConnectionFactory connectionFactory) {
        if (connectionFactory != null) {
            return new RabbitTemplate(connectionFactory);
        } else {
            return null;
        }
    }
}

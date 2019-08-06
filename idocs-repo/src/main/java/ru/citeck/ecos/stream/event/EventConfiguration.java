package ru.citeck.ecos.stream.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.events.EventConnection;

/**
 * @author Roman Makarskiy
 */
@Configuration
public class EventConfiguration {

    @Value("${event.server.host}")
    private String HOST;

    @Value("${event.server.port}")
    private int PORT;

    @Value("${event.server.username}")
    private String USERNAME;

    @Value("${event.server.password}")
    private String PASSWORD;

    //TODO: move tenant id to ecos config?
    @Value("event.server.tenant.id")
    private String TENANT_ID;

    @Bean
    public EventConnection eventConnection() {
        return new EventConnection.Builder()
                .host(HOST)
                .port(PORT)
                .username(USERNAME)
                .password(PASSWORD)
                .tenantId(TENANT_ID)
                .build();
    }

}

package ru.citeck.ecos.node;

import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;

public class AlfNodeContentPathRegistrar {

    private Map<String, String> pathByType = Collections.emptyMap();
    private AlfNodeContentPathRegistry registry;

    @Autowired
    public AlfNodeContentPathRegistrar(AlfNodeContentPathRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, String> entry : pathByType.entrySet()) {
            String type = entry.getKey();
            String path = entry.getValue();
            registry.register(QName.createQName(type), info -> path);
        }
    }

    public void setContentPath(Map<String, String> pathByType) {
        boolean isConfigInvalid = pathByType.entrySet().stream().anyMatch(e -> !e.getKey().startsWith("{"));
        if (isConfigInvalid) {
            throw new IllegalArgumentException("Short QNames are not supported! " +
                    "Please, specify a full form like " +
                    "{http://www.alfresco.org/model/content/1.0}content\n" + pathByType);
        }
        this.pathByType = pathByType;
    }
}

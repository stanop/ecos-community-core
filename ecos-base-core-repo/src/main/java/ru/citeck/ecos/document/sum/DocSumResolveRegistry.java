package ru.citeck.ecos.document.sum;

import lombok.extern.log4j.Log4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Makarskiy
 */
@Log4j
@Component
public class DocSumResolveRegistry implements ApplicationContextAware {

    private final NodeService nodeService;

    private ApplicationContext applicationContext;
    private HashMap<QName, DocSumResolver> resolvers = new HashMap<>();

    @Autowired
    public DocSumResolveRegistry(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostConstruct
    public void initialize() {
        Map<String, DocSumResolver> beansOfType = applicationContext.getBeansOfType(DocSumResolver.class);
        beansOfType.forEach((s, docSumResolver) -> {
            QName docType = docSumResolver.getDocType();
            if (docType == null) {
                throw new IllegalStateException(String.format("Cannot register document sum resolver: <%s>, " +
                        "because docType is missing", s));
            }

            resolvers.put(docType, docSumResolver);

            log.info(String.format("Register document sum resolver: <%s>, type: <%s>", s, docType));
        });
    }

    DocSumResolver get(NodeRef document) {
        QName type = nodeService.getType(document);
        return resolvers.get(type);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

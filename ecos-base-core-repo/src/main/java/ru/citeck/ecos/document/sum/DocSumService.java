package ru.citeck.ecos.document.sum;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Roman Makarskiy
 */
@Service
public class DocSumService {

    private final DocSumResolveRegistry docSumResolveRegistry;

    @Autowired
    public DocSumService(DocSumResolveRegistry docSumResolveRegistry) {
        this.docSumResolveRegistry = docSumResolveRegistry;
    }

    public BigDecimal getSum(NodeRef document) {
        DocSumResolver resolver = docSumResolveRegistry.get(document);
        if (resolver == null) {
            return null;
        }

        return resolver.resolve(document);
    }
}

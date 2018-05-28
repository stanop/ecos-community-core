package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.InputStream;

/**
 * @author Roman Makarskiy
 */
public interface FlowableModelerService {
    void importProcessModel(NodeRef nodeRef);

    void importProcessModel(InputStream inputStream);

    void importProcessModel();

    boolean importIsPossible();
}
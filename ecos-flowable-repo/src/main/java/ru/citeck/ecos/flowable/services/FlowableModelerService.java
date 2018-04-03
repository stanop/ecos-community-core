package ru.citeck.ecos.flowable.services;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.InputStream;

/**
 * @author Roman Makarskiy
 */
public interface FlowableModelerService {
    public void importProcessModel(NodeRef nodeRef);

    public void importProcessModel(InputStream inputStream);

    public void importProcessModel();

    public boolean importIsPossible();
}
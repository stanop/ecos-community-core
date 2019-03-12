package ru.citeck.ecos.user;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Map;

public interface UserAutoCreationService {

    /**
     * Creates user with given properties.
     * Mandatory properties - firstName, lastName, middleName
     * If some properties is missing, fill it with default props
     * If userName is missing, generates one.
     * @param properties properties of created user
     * @return nodeRef, that was created or updated.
     */
    public NodeRef createPerson(Map<QName, Object> properties);

    /**
     * Creates user with given properties as a child of given group.
     * Mandatory properties - firstName, lastName, middleName
     * If some properties is missing, fill it with default props
     * If userName is missing, generates one.
     * @param properties properties of created user
     * @param group person group NodeRef
     * @return nodeRef, that was created or updated.
     */
    public NodeRef createPerson(Map<QName, Object> properties, NodeRef group);

}

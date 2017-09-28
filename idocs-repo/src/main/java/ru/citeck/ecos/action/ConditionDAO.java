package ru.citeck.ecos.action;

import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface ConditionDAO {

    public abstract NodeRef save(ActionCondition condition, NodeRef parent,
            QName childAssocType);

    public abstract void save(ActionCondition condition, NodeRef nodeRef);

    public abstract ActionCondition readCondition(NodeRef nodeRef);

    public abstract List<ActionCondition> readConditions(NodeRef parent,
            QName childAssocType);

}
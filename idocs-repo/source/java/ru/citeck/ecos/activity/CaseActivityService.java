package ru.citeck.ecos.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface CaseActivityService {

    /**
     * Start activity
     * @param activityRef activity node reference
     */
    void startActivity(NodeRef activityRef);

    /**
     * Stop activity
     * @param activityRef activity node reference
     */
    void stopActivity(NodeRef activityRef);

    /**
     * @param nodeRef document with activities in children
     * @param assocName association name
     */
    List<NodeRef> getActivities(NodeRef nodeRef, QName assocName);

    NodeRef getNotActivityParent(NodeRef activityRef);
}

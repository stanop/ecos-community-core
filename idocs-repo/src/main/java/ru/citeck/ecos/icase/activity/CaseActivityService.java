package ru.citeck.ecos.icase.activity;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

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
     * Recursively resets all activities in children and nodeRef if it is an activity
     * @param nodeRef activity or document with activities in children
     */
    void reset(NodeRef nodeRef);

    /**
     * @param nodeRef node with activities in children
     */
    List<NodeRef> getActivities(NodeRef nodeRef);

    /**
     * @param nodeRef node with activities in children
     * @param type activity type
     */
    List<NodeRef> getActivities(NodeRef nodeRef, QNamePattern type);

    /**
     * @param nodeRef node with activities in children
     * @param assocType association type between node and activities in children
     * @param type activity type
     */
    List<NodeRef> getActivities(NodeRef nodeRef, QName assocType, QNamePattern type);

    /**
     * Get all started activities of node
     *@param nodeRef  node with activities in children
     */
    List<NodeRef> getStartedActivities(NodeRef nodeRef);

    /**
     * Get activity by title
     *
     * @param nodeRef node with activities in children
     * @param title   title of activity
     */
    NodeRef getActivityByTitle(NodeRef nodeRef, String title);

    /**
     * @param activityRef activity node reference
     * @return document which is owner of activity
     */
    NodeRef getDocument(NodeRef activityRef);

    /**
     * Set new parent
     * @param activityRef activity node reference. Mandatory parameter.
     * @param newParent new parent reference. Can be any node with aspect "activ:hasActivities". Mandatory parameter.
     */
    void setParent(NodeRef activityRef, NodeRef newParent);

    /**
     * Set new parent
     * @param activityRef activity node reference. Mandatory parameter.
     * @param newIndex Index define activities order when "getActivities" method called.
     *                 If this value less than zero, than activity moved to the beginning.
     *                 If this value greater or equal to activities count, than activity moved to the end.
     *                 In other case activity position in result list of "getActivities" equals to this parameter.
     */
    void setIndex(NodeRef activityRef, int newIndex);

    /**
     * Check there is active activities in children
     * @param activityRef activity node reference. Mandatory parameter.
     */
    boolean hasActiveChildren(NodeRef activityRef);

    /**
     * Is activity active
     * @param activityRef activity node reference. Mandatory parameter.
     */
    boolean isActive(NodeRef activityRef);
}

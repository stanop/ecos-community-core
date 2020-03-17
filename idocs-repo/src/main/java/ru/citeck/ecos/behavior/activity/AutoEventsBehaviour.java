package ru.citeck.ecos.behavior.activity;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.alfresco.CaseActivityPolicies.OnChildrenIndexChangedPolicy;
import ru.citeck.ecos.icase.activity.service.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.StagesModel;
import ru.citeck.ecos.service.EcosCoreServices;
import ru.citeck.ecos.utils.AlfActivityUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@DependsOn("idocs.dictionaryBootstrap")
public class AutoEventsBehaviour extends AbstractBehaviour
                                 implements OnChildrenIndexChangedPolicy,
                                            OnCreateChildAssociationPolicy,
                                            OnDeleteChildAssociationPolicy,
                                            OnUpdatePropertiesPolicy {

    private static final QName TYPE_ACTIVITY = ActivityModel.TYPE_ACTIVITY;

    private CaseActivityService caseActivityService;

    @Autowired
    private AlfActivityUtils alfActivityUtils;

    @Override
    protected void beforeInit() {
        setClassName(ActivityModel.ASPECT_HAS_ACTIVITIES);
        setAssocName(ActivityModel.ASSOC_ACTIVITIES);
        caseActivityService = EcosCoreServices.getCaseActivityService(serviceRegistry);
    }

    @PolicyMethod(policy = OnChildrenIndexChangedPolicy.class,
                  frequency = NotificationFrequency.TRANSACTION_COMMIT, runAsSystem = true)
    public void onChildrenIndexChanged(NodeRef activityRef) {
        updateChildren(activityRef);
    }

    @PolicyMethod(policy = OnDeleteChildAssociationPolicy.class,
                  frequency = NotificationFrequency.TRANSACTION_COMMIT, runAsSystem = true)
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef) {
        updateChildren(childAssocRef.getParentRef());
    }

    @PolicyMethod(policy = OnCreateChildAssociationPolicy.class,
                  frequency = NotificationFrequency.TRANSACTION_COMMIT, runAsSystem = true)
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
        updateChildren(childAssocRef.getParentRef());
    }

    @PolicyMethod(policy = OnUpdatePropertiesPolicy.class, classField = "TYPE_ACTIVITY",
                  frequency = NotificationFrequency.TRANSACTION_COMMIT, runAsSystem = true)
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

        Boolean autoEventsBefore = (Boolean) before.get(ActivityModel.PROP_AUTO_EVENTS);
        Boolean autoEventsAfter = (Boolean) after.get(ActivityModel.PROP_AUTO_EVENTS);

        if (!Objects.equals(autoEventsBefore, autoEventsAfter)) {
            updateActivity(nodeRef);
        }
    }

    private void updateChildren(NodeRef parentNodeRef) {
        ActivityRef parentRef = alfActivityUtils.composeActivityRef(parentNodeRef);
        List<CaseActivity> activities = caseActivityService.getActivities(parentRef);

        for (int i = 0; i < activities.size(); i++) {

            NodeRef beforeNodeRef = null;
            if (i > 0) {
                beforeNodeRef = alfActivityUtils.getActivityNodeRef(activities.get(i - 1).getActivityRef());
            }

            NodeRef activityNodeRef = alfActivityUtils.getActivityNodeRef(activities.get(i).getActivityRef());
            updateActivity(parentNodeRef, activityNodeRef, beforeNodeRef);
        }
    }

    private void updateActivity(NodeRef activityNodeRef) {
        ActivityRef activityRef = alfActivityUtils.composeActivityRef(activityNodeRef);
        ActivityRef parentRef = alfActivityUtils.getParentActivityRef(activityRef);

        NodeRef parentNodeRef = alfActivityUtils.getActivityNodeRef(parentRef);

        List<CaseActivity> activities = caseActivityService.getActivities(parentRef);
        CaseActivity selectedActivity = caseActivityService.getActivity(activityRef);
        int idx = activities.indexOf(selectedActivity);

        NodeRef beforeNodeRef = null;
        if (idx > 0) {
            beforeNodeRef = alfActivityUtils.getActivityNodeRef(activities.get(idx - 1).getActivityRef());
        }
        updateActivity(parentNodeRef, activityNodeRef, beforeNodeRef);
    }

    private void updateActivity(NodeRef parentRef, NodeRef activityRef, NodeRef beforeRef) {

        if (isAutoEvents(activityRef)) {

            if (beforeRef != null) {

                updateActivityEventsImpl(activityRef, beforeRef,
                                         ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS,
                                         ICaseEventModel.TYPE_ACTIVITY_STOPPED_EVENT);
            } else {

                QName type = nodeService.getType(parentRef);

                if (dictionaryService.isSubClass(type, ActivityModel.TYPE_ACTIVITY)) {

                    updateActivityEventsImpl(activityRef, parentRef,
                                             ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS,
                                             ICaseEventModel.TYPE_ACTIVITY_STARTED_EVENT);

                } else {

                    updateActivityEventsImpl(activityRef, parentRef,
                                             ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS,
                                             ICaseEventModel.TYPE_CASE_CREATED);
                }
            }

            QName activityType = nodeService.getType(activityRef);

            if (dictionaryService.isSubClass(activityType, StagesModel.TYPE_STAGE)) {

                updateActivityEventsImpl(activityRef, activityRef,
                                         ICaseEventModel.ASSOC_ACTIVITY_END_EVENTS,
                                         ICaseEventModel.TYPE_STAGE_CHILDREN_STOPPED);
            }
        }
    }

    private void updateActivityEventsImpl(NodeRef activityRef, NodeRef eventSource, QName assocName, QName eventType) {

        List<ChildAssociationRef> eventsAssocs;
        eventsAssocs = nodeService.getChildAssocs(activityRef, assocName, q -> true);

        NodeRef eventRef = null;

        for (ChildAssociationRef childAssoc : eventsAssocs) {

            NodeRef childRef = childAssoc.getChildRef();

            if (eventRef == null) {
                QName type = nodeService.getType(childRef);
                if (eventType.equals(type)) {
                    eventRef = childRef;
                    continue;
                }
            }
            nodeService.addAspect(childRef, ContentModel.ASPECT_TEMPORARY, new HashMap<>());
            nodeService.deleteNode(childRef);
        }

        if (eventRef == null) {

            eventRef = nodeService.createNode(activityRef,
                                              assocName,
                                              ActivityModel.PROP_AUTO_EVENTS,
                                              eventType).getChildRef();

            nodeService.createAssociation(eventRef, eventSource, EventModel.ASSOC_EVENT_SOURCE);

        } else {

            List<AssociationRef> eventSourceAssocs = nodeService.getTargetAssocs(eventRef, EventModel.ASSOC_EVENT_SOURCE);

            if (!eventSourceAssocs.isEmpty()) {

                NodeRef targetRef = eventSourceAssocs.get(0).getTargetRef();

                if (!targetRef.equals(eventSource)) {

                    nodeService.removeAssociation(eventRef, targetRef, EventModel.ASSOC_EVENT_SOURCE);
                    nodeService.createAssociation(eventRef, eventSource, EventModel.ASSOC_EVENT_SOURCE);
                }
            } else {
                nodeService.createAssociation(eventRef, eventSource, EventModel.ASSOC_EVENT_SOURCE);
            }
        }
    }

    private boolean isAutoEvents(NodeRef activityRef) {
        Boolean isAutoEvents = (Boolean) nodeService.getProperty(activityRef, ActivityModel.PROP_AUTO_EVENTS);
        return Boolean.TRUE.equals(isAutoEvents);
    }
}

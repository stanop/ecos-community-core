package ru.citeck.ecos.behavior.activity;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.*;
import org.alfresco.repo.policy.Behaviour.*;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.icase.activity.CaseActivityPolicies.*;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.EventModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.model.StagesModel;
import ru.citeck.ecos.service.EcosCoreServices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AutoEventsBehaviour extends AbstractBehaviour
                                 implements OnChildrenIndexChangedPolicy,
                                            OnCreateChildAssociationPolicy,
                                            OnDeleteChildAssociationPolicy,
                                            OnUpdatePropertiesPolicy {

    private static final QName TYPE_ACTIVITY = ActivityModel.TYPE_ACTIVITY;

    private CaseActivityService caseActivityService;

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

    private void updateChildren(NodeRef parentRef) {

        List<NodeRef> activities = caseActivityService.getActivities(parentRef);

        for (int i = 0; i < activities.size(); i++) {
            NodeRef beforeRef = i > 0 ? activities.get(i - 1) : null;
            updateActivity(parentRef, activities.get(i), beforeRef);
        }
    }

    private void updateActivity(NodeRef activityRef) {
        NodeRef parentRef = nodeService.getPrimaryParent(activityRef).getParentRef();
        List<NodeRef> activities = caseActivityService.getActivities(parentRef);
        int idx = activities.indexOf(activityRef);
        NodeRef beforeRef = idx > 0 ? activities.get(idx - 1) : null;
        updateActivity(parentRef, activityRef, beforeRef);
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

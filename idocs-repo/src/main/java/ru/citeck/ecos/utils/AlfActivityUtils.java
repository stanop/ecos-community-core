package ru.citeck.ecos.utils;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.alfresco.AlfrescoCaseActivityDelegate;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records2.RecordRef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("alfActivityUtils")
public class AlfActivityUtils {

    private AlfrescoCaseActivityDelegate alfrescoCaseActivityDelegate;
    private DictionaryService dictionaryService;
    private NodeService nodeService;

    public List<CaseActivity> getActivities(NodeRef nodeRef, QName assocQName, boolean recurse) {
        RecordRef documentId = getDocumentId(nodeRef);
        return getActivitiesImpl(nodeRef, documentId, assocQName, recurse);
    }

    public ActivityRef composeActivityRef(NodeRef activityNodeRef) {
        RecordRef documentRef = getDocumentId(activityNodeRef);
        return composeActivityRef(activityNodeRef, documentRef);
    }

    public ActivityRef getParentActivityRef(ActivityRef activityRef) {
        NodeRef activityNodeRef = getActivityNodeRef(activityRef);
        NodeRef parentRef = nodeService.getPrimaryParent(activityNodeRef).getParentRef();
        if (nodeService.hasAspect(parentRef, ICaseModel.ASPECT_CASE)) {
            ActivityRef.of(CaseServiceType.ALFRESCO, activityRef.getProcessId(), ActivityRef.ROOT_ID);
        }
        return ActivityRef.of(CaseServiceType.ALFRESCO, activityRef.getProcessId(), parentRef.toString());
    }

    public NodeRef getActivityNodeRef(ActivityRef activityRef) {
        return activityRef.isRoot() ?
            RecordsUtils.toNodeRef(activityRef.getProcessId()) :
            new NodeRef(activityRef.getId());
    }

    public EventRef composeEventRef(NodeRef eventRef) {
        RecordRef documentRef = getDocumentId(eventRef);
        return EventRef.of(CaseServiceType.ALFRESCO, documentRef, eventRef.toString());
    }

    public NodeRef getEventNodeRef(EventRef eventRef) {
        return eventRef.isRoot() ?
            RecordsUtils.toNodeRef(eventRef.getProcessId()) :
            new NodeRef(eventRef.getId());
    }

    public RecordRef getDocumentId(NodeRef activityNodeRef) {
        if (nodeService.hasAspect(activityNodeRef, ICaseModel.ASPECT_CASE)) {
            return RecordRef.valueOf(activityNodeRef.toString());
        }

        NodeRef parentRef = nodeService.getPrimaryParent(activityNodeRef).getParentRef();
        while (parentRef != null
            && RepoUtils.isSubType(parentRef, ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService)) {
            parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
        }

        return parentRef != null ? RecordRef.valueOf(parentRef.toString()) : null;
    }

    private List<CaseActivity> getActivitiesImpl(NodeRef nodeRef, RecordRef documentId, QName assocQName, boolean recurse) {
        List<NodeRef> children = RepoUtils.getChildrenByAssoc(nodeRef, assocQName, nodeService);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }

        List<Pair<NodeRef, Integer>> indexedChildren = new ArrayList<>(children.size());
        for (NodeRef childRef : children) {
            Integer index = (Integer) nodeService.getProperty(childRef, ActivityModel.PROP_INDEX);
            indexedChildren.add(new Pair<>(childRef, index != null ? index : 0));
        }

        indexedChildren.sort(Comparator.comparingInt(Pair::getSecond));

        List<CaseActivity> result = new ArrayList<>(indexedChildren.size());
        for (Pair<NodeRef, Integer> child : indexedChildren) {
            NodeRef childActivityNodeRef = child.getFirst();
            ActivityRef childActivityRef = composeActivityRef(childActivityNodeRef, documentId);
            CaseActivity childActivity = alfrescoCaseActivityDelegate.getActivity(childActivityRef);
            result.add(childActivity);
        }

        if (recurse) {
            for (NodeRef childRef : children) {
                result.addAll(getActivitiesImpl(childRef, documentId, assocQName, true));
            }
        }

        return result;
    }

    private ActivityRef composeActivityRef(NodeRef activityNodeRef, RecordRef documentRef) {
        if (nodeService.hasAspect(activityNodeRef, ICaseModel.ASPECT_CASE)) {
            ActivityRef.of(CaseServiceType.ALFRESCO, documentRef, ActivityRef.ROOT_ID);
        }
        return ActivityRef.of(CaseServiceType.ALFRESCO, documentRef, activityNodeRef.toString());
    }

    @Autowired
    public void setAlfrescoCaseActivityDelegate(AlfrescoCaseActivityDelegate alfrescoCaseActivityDelegate) {
        this.alfrescoCaseActivityDelegate = alfrescoCaseActivityDelegate;
    }

    @Autowired
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}

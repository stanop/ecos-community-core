package ru.citeck.ecos.utils;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.dto.EventRef;
import ru.citeck.ecos.icase.activity.service.ActivityCommonService;

@Component
public class ActivityUtilsJS {

    private ActivityCommonService activityCommonService;
    private AlfActivityUtils alfActivityUtils;

    @Autowired
    public ActivityUtilsJS(ActivityCommonService activityCommonService, AlfActivityUtils alfActivityUtils) {
        this.activityCommonService = activityCommonService;
        this.alfActivityUtils = alfActivityUtils;
    }

    public ActivityRef getActivityRef(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ActivityRef) {
            return (ActivityRef) object;
        }
        if (object instanceof CaseActivity) {
            return ((CaseActivity) object).getActivityRef();
        }
        if (object instanceof NodeRef) {
            NodeRef nodeRef = (NodeRef) object;
            if (activityCommonService.isRoot(nodeRef)) {
                return activityCommonService.composeRootActivityRef(nodeRef);
            }
            return alfActivityUtils.composeActivityRef(nodeRef);
        }
        if (object instanceof ScriptNode) {
            NodeRef nodeRef = ((ScriptNode) object).getNodeRef();
            if (activityCommonService.isRoot(nodeRef)) {
                return activityCommonService.composeRootActivityRef(nodeRef);
            }
            return alfActivityUtils.composeActivityRef(nodeRef);
        }

        ActivityRef activityRef = activityCommonService.composeActivityRef(object.toString());
        if (activityRef != null) {
            return activityRef;
        }

        if (NodeRef.isNodeRef(object.toString())) {
            NodeRef nodeRef = new NodeRef(object.toString());
            if (activityCommonService.isRoot(nodeRef)) {
                return activityCommonService.composeRootActivityRef(nodeRef);
            }
            return alfActivityUtils.composeActivityRef(nodeRef);
        }

        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to ActivityRef. " +
                "Source: " + object.toString());
    }

    public EventRef getEventRef(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof EventRef) {
            return (EventRef) object;
        }
        if (object instanceof NodeRef) {
            return alfActivityUtils.composeEventRef((NodeRef) object);
        }
        if (object instanceof ScriptNode) {
            return alfActivityUtils.composeEventRef(((ScriptNode) object).getNodeRef());
        }

        EventRef eventRef = activityCommonService.composeEventRef(object.toString());
        if (eventRef != null) {
            return eventRef;
        }

        if (NodeRef.isNodeRef(object.toString())) {
            return alfActivityUtils.composeEventRef(new NodeRef(object.toString()));
        }

        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to EventRef. " +
                "Source: " + object.toString());
    }

}

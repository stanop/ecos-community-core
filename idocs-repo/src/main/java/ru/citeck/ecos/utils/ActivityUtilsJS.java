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
        if (object instanceof String) {
            if (NodeRef.isNodeRef((String) object)) {
                NodeRef nodeRef = new NodeRef((String) object);
                if (activityCommonService.isRoot(nodeRef)) {
                    return activityCommonService.composeRootActivityRef(nodeRef);
                }
                return alfActivityUtils.composeActivityRef(nodeRef);
            }
        }

        // Try to stringify and parse result
        ActivityRef activityRef = activityCommonService.composeActivityRef(object.toString());
        if (activityRef != null) {
            return activityRef;
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
        if (object instanceof String) {
            if (NodeRef.isNodeRef((String) object)) {
                return alfActivityUtils.composeEventRef(new NodeRef((String) object));
            }
        }

        // Try to stringify and parse result
        EventRef eventRef = activityCommonService.composeEventRef(object.toString());
        if (eventRef != null) {
            return eventRef;
        }

        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to EventRef. " +
                "Source: " + object.toString());
    }

}

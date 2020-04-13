package ru.citeck.ecos.utils;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.icase.activity.dto.ActivityRef;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.dto.EventRef;

public class ActivityUtilsJS {

    //TODO: algorithm for eproc activities is missed?
    public static ActivityRef getActivityRef(Object object, AlfActivityUtils alfActivityUtils) {
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
            return alfActivityUtils.composeActivityRef((NodeRef) object);
        }
        if (object instanceof ScriptNode) {
            return alfActivityUtils.composeActivityRef(((ScriptNode) object).getNodeRef());
        }
        if (object instanceof String) {
            if (NodeRef.isNodeRef((String) object)) {
                return alfActivityUtils.composeActivityRef(new NodeRef((String) object));
            }
        }
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to ActivityRef. " +
            "Source: " + object.toString());
    }

    //TODO: algorithm for eproc events is missed?
    public static EventRef getEventRef(Object object, AlfActivityUtils alfActivityUtils) {
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
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to EventRef. " +
            "Source: " + object.toString());
    }

}

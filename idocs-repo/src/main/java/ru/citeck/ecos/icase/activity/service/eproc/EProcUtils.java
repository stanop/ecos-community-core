package ru.citeck.ecos.icase.activity.service.eproc;

import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.records2.RecordRef;

public class EProcUtils {

    public static ActivityRef composeActivityRef(ActivityInstance activityInstance, RecordRef caseRef) {
        return ActivityRef.of(CaseServiceType.EPROC, caseRef, activityInstance.getId());
    }

    public static <T> T getDefAttribute(ActivityDefinition definition, String key, Class<T> clazz) {
        ObjectData data = definition.getData();
        if (data == null) {
            return null;
        }

        return data.get(key, clazz);
    }

    public static <T> T getInsAttribute(ActivityInstance instance, String key, Class<T> clazz) {
        ObjectData variables = instance.getVariables();
        if (variables == null) {
            return null;
        }

        return variables.get(key, clazz);
    }

    public static String getAnyAttribute(ActivityInstance instance, String key) {
        return getAnyAttribute(instance, key, String.class);
    }

    public static <T> T getAnyAttribute(ActivityInstance instance, String key, Class<T> clazz) {
        T result = getInsAttribute(instance, key, clazz);
        if (result == null) {
            result = getDefAttribute(instance.getDefinition(), key, clazz);
        }
        return result;
    }

    public static void setAttribute(ActivityInstance instance, String key, Object value) {
        ObjectData variables = instance.getVariables();
        if (variables == null) {
            variables = new ObjectData();
            instance.setVariables(variables);
        }

        if (value == null) {
            variables.remove(key);
        } else {
            variables.set(key, value);
        }
    }

    public static boolean isRoot(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.ROOT;
    }

    public static boolean isStage(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.STAGE;
    }

    public static boolean isAction(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.ACTION;
    }

    public static boolean isUserTask(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.USER_TASK;
    }

    public static boolean isProcessTask(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.PROCESS_TASK;
    }

    public static boolean isTimer(ActivityDefinition activityDefinition) {
        return activityDefinition.getType() == ActivityType.TIMER;
    }

}

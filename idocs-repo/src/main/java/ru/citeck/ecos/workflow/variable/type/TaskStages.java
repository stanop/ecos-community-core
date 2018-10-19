package ru.citeck.ecos.workflow.variable.type;

import java.util.ArrayList;
import java.util.Collection;

public class TaskStages extends ArrayList<TaskConfigs> implements EcosPojoType {

    public TaskStages() {
    }

    public TaskStages(Collection<TaskConfigs> other) {
        super(other);
    }
}

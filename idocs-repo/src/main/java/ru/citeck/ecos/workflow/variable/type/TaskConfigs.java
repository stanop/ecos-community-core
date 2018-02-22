package ru.citeck.ecos.workflow.variable.type;

import java.util.ArrayList;
import java.util.Collection;

public class TaskConfigs extends ArrayList<TaskConfig> implements EcosPojoType {

    public TaskConfigs() {
    }

    public TaskConfigs(Collection<TaskConfig> other) {
        super(other);
    }
}

package ru.citeck.ecos.icase.activity.create.provider;

import ru.citeck.ecos.icase.activity.create.dto.ActivityCreateVariant;

import java.util.List;

public interface CreateVariantsProvider {

    String ACTIVITI_TASK_TITLE_PREFIX = "A: ";
    String FLOWABLE_TASK_TITLE_PREFIX = "F: ";
    String FLOWABLE_ENGINE_PREFIX = "flowable";

    List<ActivityCreateVariant> getCreateVariants();

}


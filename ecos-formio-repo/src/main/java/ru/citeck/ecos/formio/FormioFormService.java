package ru.citeck.ecos.formio;

import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.formio.provider.FormProvider;
import ru.citeck.ecos.records.RecordRef;

import java.util.List;
import java.util.Optional;

public interface FormioFormService {

    Optional<FormioFormModel> getFormByKey(String formKey);

    Optional<FormioFormModel> getFormByKey(List<String> formKeys);

    Optional<FormioFormModel> getFormByRecord(RecordRef record, Boolean isViewMode);

    Optional<FormioFormModel> getFormById(String id);

    void save(FormioFormModel model);

    FormioFormModel getDefault();

    boolean hasForm(RecordRef record, Boolean isViewMode);

    void register(FormProvider formProvider);
}

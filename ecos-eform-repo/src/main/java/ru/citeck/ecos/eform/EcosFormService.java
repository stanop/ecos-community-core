package ru.citeck.ecos.eform;

import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.eform.provider.FormProvider;
import ru.citeck.ecos.records2.RecordRef;

import java.util.List;
import java.util.Optional;

public interface EcosFormService {

    Optional<EcosFormModel> getFormByKey(String formKey);

    Optional<EcosFormModel> getFormByKey(List<String> formKeys);

    List<EcosFormModel> getFormsByKeys(List<String> formKeys);

    Optional<EcosFormModel> getFormByRecord(RecordRef record, Boolean isViewMode);

    Optional<EcosFormModel> getFormById(String id);

    String save(EcosFormModel model);

    EcosFormModel getDefault();

    boolean hasForm(RecordRef record, Boolean isViewMode);

    void register(FormProvider formProvider);
}

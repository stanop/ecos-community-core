package ru.citeck.ecos.formio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.formio.model.FormioFormModel;
import ru.citeck.ecos.formio.provider.FormProvider;
import ru.citeck.ecos.formio.provider.MutableFormProvider;
import ru.citeck.ecos.graphql.meta.annotation.MetaAtt;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;

import java.util.*;

public class FormioFormServiceImpl implements FormioFormService {

    private static final String DEFAULT_KEY = "DEFAULT";

    private Set<FormProvider> providers = new TreeSet<>(Comparator.comparing(FormProvider::getOrder));
    private MutableFormProvider newFormsStore;

    private RecordsService recordsService;

    @Override
    public FormioFormModel getDefault() {
        return getFormByKey(DEFAULT_KEY).orElseThrow(() -> new IllegalStateException("Default form is not found!"));
    }

    @Override
    public Optional<FormioFormModel> getFormByKey(String formKey) {

        FormioFormModel form = null;

        for (FormProvider provider : providers) {
            form = provider.getFormByKey(formKey);
            if (form != null) {
                break;
            }
        }

        return Optional.ofNullable(form);
    }

    @Override
    public Optional<FormioFormModel> getFormByKey(List<String> formKeys) {

        return formKeys.stream()
                       .map(this::getFormByKey)
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .findFirst();
    }

    @Override
    public Optional<FormioFormModel> getFormByRecord(RecordRef record, Boolean isViewMode) {

        if (isViewMode == null) {
            isViewMode = false;
        }

        if (isViewMode) {

            ViewFormKeys keys = recordsService.getMeta(record, ViewFormKeys.class);
            Optional<FormioFormModel> form = getFormByKey(keys.getViewKeys());
            if (!form.isPresent()) {
                form = getFormByKey(keys.getKeys());
            }
            return form;

        } else {

            FormKeys keys = recordsService.getMeta(record, FormKeys.class);
            return getFormByKey(keys.getKeys());
        }
    }

    @Override
    public Optional<FormioFormModel> getFormById(String id) {

        for (FormProvider provider : providers) {

            FormioFormModel form = provider.getFormById(id);
            if (form != null) {
                return Optional.of(form);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(FormioFormModel model) {

        for (FormProvider provider : providers) {

            FormioFormModel form = provider.getFormById(model.getId());
            if (form != null) {
                if (provider instanceof MutableFormProvider) {
                    ((MutableFormProvider) provider).save(model);
                    return;
                }
            }
        }

        newFormsStore.create(model);
    }

    @Override
    public boolean hasForm(RecordRef record, Boolean isViewMode) {
        return getFormByRecord(record, isViewMode).isPresent();
    }

    @Override
    public void register(FormProvider formProvider) {
        providers.add(formProvider);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    public void setNewFormsStore(MutableFormProvider newFormsStore) {
        this.newFormsStore = newFormsStore;
    }

    public static class FormKeys {

        @MetaAtt(RecordConstants.ATT_FORM_KEY)
        @Getter @Setter private List<String> keys;
    }

    public static class ViewFormKeys extends FormKeys {

        @MetaAtt(RecordConstants.ATT_VIEW_FORM_KEY)
        @Getter @Setter private List<String> viewKeys;
    }
}

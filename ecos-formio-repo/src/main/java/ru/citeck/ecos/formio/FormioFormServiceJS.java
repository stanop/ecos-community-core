package ru.citeck.ecos.formio;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JsUtils;

public class FormioFormServiceJS extends AlfrescoScopableProcessorExtension {

    private JsUtils jsUtils;
    private FormioFormService formioFormService;

    public boolean hasForm(Object record, Object mode) {

        RecordRef recordRef = jsUtils.toJava(record, RecordRef.class);
        Boolean formMode = jsUtils.toJava(mode, Boolean.class);

        return formioFormService.hasForm(recordRef, formMode);
    }

    public boolean hasForm(Object record) {
        RecordRef recordRef = jsUtils.toJava(record, RecordRef.class);
        return formioFormService.hasForm(recordRef, null);
    }

    @Autowired
    public void setFormioFormService(FormioFormService formioFormService) {
        this.formioFormService = formioFormService;
    }

    @Autowired
    public void setJsUtils(JsUtils jsUtils) {
        this.jsUtils = jsUtils;
    }
}

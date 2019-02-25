package ru.citeck.ecos.eform;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JsUtils;

public class EcosFormServiceJS extends AlfrescoScopableProcessorExtension {

    private JsUtils jsUtils;
    private EcosFormService eformFormService;

    public boolean hasForm(Object record, Object mode) {

        RecordRef recordRef = jsUtils.toJava(record, RecordRef.class);
        Boolean formMode = jsUtils.toJava(mode, Boolean.class);

        return eformFormService.hasForm(recordRef, formMode);
    }

    public boolean hasForm(Object record) {
        RecordRef recordRef = jsUtils.toJava(record, RecordRef.class);
        return eformFormService.hasForm(recordRef, null);
    }

    @Autowired
    public void setEcosFormService(EcosFormService eformFormService) {
        this.eformFormService = eformFormService;
    }

    @Autowired
    public void setJsUtils(JsUtils jsUtils) {
        this.jsUtils = jsUtils;
    }
}

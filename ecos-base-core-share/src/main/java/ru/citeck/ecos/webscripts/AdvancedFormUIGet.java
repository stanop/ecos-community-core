package ru.citeck.ecos.webscripts;

import org.alfresco.web.config.forms.FormConfigElement;
import org.alfresco.web.config.forms.Mode;
import org.alfresco.web.scripts.forms.FormUIGet;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.*;
import java.util.List;
import java.util.Map;

/**
 * Advanced form ui get webscript
 */
public class AdvancedFormUIGet extends FormUIGet {

    /**
     * Constants
     */
    private static final String DEFAULT_TASK_VIEW = "flowable$defaultTaskView";
    private static final String WORKFLOW_ITEM_KIND = "workflow";

    /**
     * Generate model
     *
     * @param itemKind Item kind
     * @param itemId   Item id
     * @param request  Web script request
     * @param status   Status
     * @param cache    Cache
     * @return Map of generated model attributes
     */
    @Override
    protected Map<String, Object> generateModel(String itemKind, String itemId, WebScriptRequest request, Status status, Cache cache) {
        Map<String, Object> model = null;
        String modeParam = this.getParameter(request, "mode", "edit");
        String formId = this.getParameter(request, "formId");
        Mode mode = Mode.modeFromString(modeParam);

        FormConfigElement formConfig = this.getFormConfig(itemId, formId, itemKind);
        List<String> visibleFields = this.getVisibleFields(mode, formConfig);
        Response formSvcResponse = this.retrieveFormDefinition(itemKind, itemId, visibleFields, formConfig);
        if (formSvcResponse.getStatus().getCode() == 200) {
            model = this.generateFormModel(request, mode, formSvcResponse, formConfig);
        } else if (formSvcResponse.getStatus().getCode() == 401) {
            status.setCode(401);
            status.setRedirect(true);
        } else {
            String errorKey = this.getParameter(request, "err");
            model = this.generateErrorModel(formSvcResponse, errorKey);
        }
        return model;
    }


    /**
     * Get config element
     *
     * @param itemId   Item id
     * @param formId   Form id
     * @param itemKind Item kind
     * @return Form config element
     */
    protected FormConfigElement getFormConfig(String itemId, String formId, String itemKind) {
        FormConfigElement config = super.getFormConfig(itemId, formId);
        if (config == null) {
            /** Load default for workflow item kind */
            if (itemKind.equals(WORKFLOW_ITEM_KIND)) {
                return super.getFormConfig(DEFAULT_TASK_VIEW, formId);
            } else {
                return null;
            }
        } else {
            return config;
        }
    }
}

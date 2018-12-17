package ru.citeck.ecos.flowable.utils;

import org.apache.commons.lang.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.common.api.FlowableObjectNotFoundException;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;

/**
 * @author Roman Makarskiy
 */
public class FlowableUtils {

    private static final String FORM_KEY_FULL_FORMAT = "form_%s_outcome";

    public static String getFullFormKey(String formKey) {
        if (StringUtils.isBlank(formKey)) {
            return null;
        }
        return String.format(FORM_KEY_FULL_FORMAT, formKey);
    }

    public static Process getProcessByDefinitionId(String processDefinitionId) {
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionId);
        if (bpmnModel == null) {
            throw new FlowableObjectNotFoundException("Cannot find bpmn model for process definition id: " +
                    processDefinitionId, BpmnModel.class);
        }
        return bpmnModel.getMainProcess();
    }

}

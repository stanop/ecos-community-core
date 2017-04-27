package ru.citeck.ecos.action;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.icase.CaseStatusService;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class SetCaseStatusActionExecuter extends ActionExecuterAbstractBase {

    public static final String NAME = "set-case-status";
    public static final String PARAM_STATUS = "status";

    private CaseStatusService caseStatusService;

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

        NodeRef status = (NodeRef) action.getParameterValue(PARAM_STATUS);
        ParameterCheck.mandatory("status", status);

        caseStatusService.setStatus(actionedUponNodeRef, status);
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_STATUS, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_STATUS)));
    }

    public void setCaseStatusService(CaseStatusService caseStatusService) {
        this.caseStatusService = caseStatusService;
    }
}

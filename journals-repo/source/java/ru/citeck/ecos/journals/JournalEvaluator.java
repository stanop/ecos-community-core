package ru.citeck.ecos.journals;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.journals.xml.Evaluator;
import ru.citeck.ecos.journals.xml.LogicJoinType;
import ru.citeck.ecos.journals.xml.Option;
import ru.citeck.ecos.service.CiteckServices;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public class JournalEvaluator {

    private static final LogicJoinType DEFAULT_LOGIC_JOIN_TYPE = LogicJoinType.AND;

    private ActionService actionService;
    private JournalService journalService;

    private ActionCondition condition;

    private NodeRef journalRef;
    private String journalId;

    public JournalEvaluator(Evaluator evaluator, String journalId, ServiceRegistry serviceRegistry) {
        this.actionService = serviceRegistry.getActionService();
        this.journalService = (JournalService) serviceRegistry.getService(CiteckServices.JOURNAL_SERVICE);
        this.journalId = journalId;
        this.condition = createCondition(evaluator);
    }

    private ActionCondition createCondition(Evaluator evaluator) {
        if (evaluator == null) return null;
        List<Evaluator> evaluators = evaluator.getEvaluator();
        ActionCondition condition;
        if (evaluators != null && !evaluators.isEmpty()) {
            CompositeActionCondition compositeCondition = actionService.createCompositeActionCondition();
            setLogicJoinType(compositeCondition, evaluator.getJoinBy());
            for (Evaluator eval : evaluators) {
                compositeCondition.addActionCondition(createCondition(eval));
            }
            condition = compositeCondition;
        } else {
            String id = evaluator.getId();
            if (StringUtils.isBlank(id)) {
                throw new IllegalStateException("Field 'id' must be specified for not composite evaluator");
            }
            condition = actionService.createActionCondition(id);
            List<Option> options = evaluator.getOption();
            for (Option option : options) {
                condition.setParameterValue(option.getName(), option.getValue());
            }
        }
        return condition;
    }

    private void setLogicJoinType(CompositeActionCondition condition, LogicJoinType joinBy) {
        if (joinBy == null) {
            joinBy = DEFAULT_LOGIC_JOIN_TYPE;
        }
        switch (joinBy) {
            case AND:
                condition.setORCondition(false);
                break;
            case OR:
                condition.setORCondition(true);
                break;
        }
    }

    private NodeRef getJournalRef() {
        if (journalRef == null) {
            journalRef = journalService.getJournalRef(journalId);
        }
        return journalRef;
    }

    public boolean evaluate() {
        return condition == null || actionService.evaluateActionCondition(condition, getJournalRef());
    }
}

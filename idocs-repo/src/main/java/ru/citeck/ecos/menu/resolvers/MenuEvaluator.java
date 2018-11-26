package ru.citeck.ecos.menu.resolvers;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.apache.commons.lang3.StringUtils;
import ru.citeck.ecos.menu.xml.Evaluator;
import ru.citeck.ecos.menu.xml.Parameter;

import java.util.List;

public class MenuEvaluator {

    private ActionService actionService;
    private ActionCondition condition;

    public MenuEvaluator(Evaluator evaluator, ActionService actionService) {
        this.actionService = actionService;
        this.condition = createCondition(evaluator);
    }

    private ActionCondition createCondition(Evaluator evaluator) {
        String id = evaluator.getId();
        if (StringUtils.isBlank(id)) {
            throw new IllegalStateException("Field 'id' must be specified for evaluator.");
        }
        condition = actionService.createActionCondition(id);
        List<Parameter> params = evaluator.getParam();
        for (Parameter param : params) {
            condition.setParameterValue(param.getName(), param.getValue());
        }
        condition.setParameterValue("userName", AuthenticationUtil.getRunAsUser());
        return condition;
    }

    public boolean evaluate() {
        return condition == null || actionService.evaluateActionCondition(condition, null);
    }

}

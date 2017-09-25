package ru.citeck.ecos.icase.timer.evaluator;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.model.CaseTimerModel;

import java.util.Date;

/**
 * @author Pavel Simonov
 */
public class ExpressionEvaluator extends Evaluator {

    @Override
    public String evaluate(NodeRef timerRef, Date fromDate, int repeatCounter) {
        return getExpression(timerRef); //TODO
    }

    @Override
    public CaseTimerModel.ExpressionType getType() {
        return CaseTimerModel.ExpressionType.EXPRESSION;
    }
}

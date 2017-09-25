package ru.citeck.ecos.icase.timer.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.quartz.CronExpression;
import ru.citeck.ecos.model.CaseTimerModel;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Pavel Simonov
 */
public class CronEvaluator extends Evaluator {

    @Override
    public CaseTimerModel.ExpressionType getType() {
        return CaseTimerModel.ExpressionType.CRON;
    }

    @Override
    public String evaluate(NodeRef timerRef, Date fromDate, int repeatCounter) {
        String expression = getExpression(timerRef);
        try {
            CronExpression cronExpression = new CronExpression(expression);
            Date nextDate = cronExpression.getNextValidTimeAfter(fromDate);
            return "R/" + ISO8601DateFormat.format(nextDate);
        } catch (ParseException e) {
            throw new AlfrescoRuntimeException("Cron parse failed. Expression: '" + expression + "'", e);
        }
    }

}

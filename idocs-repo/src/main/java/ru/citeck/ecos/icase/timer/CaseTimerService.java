package ru.citeck.ecos.icase.timer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.Interval;
import ru.citeck.ecos.event.EventService;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.icase.timer.evaluator.Evaluator;
import ru.citeck.ecos.model.CaseTimerModel.DatePrecision;
import ru.citeck.ecos.model.CaseTimerModel.ExpressionType;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.model.ICaseEventModel;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.service.EcosCoreServices;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Simonov
 */
public class CaseTimerService {

    private static final Pattern REPEAT_PATTERN = Pattern.compile("^R([0-9]*)/(.*)");

    private Map<ExpressionType, Evaluator> evaluators = new HashMap<>();
    private Map<DatePrecision, Integer> calendarCodeByPrecision = new HashMap<>();

    private NodeService nodeService;
    private EventService eventService;
    private CaseActivityService caseActivityService;

    public CaseTimerService() {
        calendarCodeByPrecision.put(DatePrecision.MONTH, Calendar.MONTH);
        calendarCodeByPrecision.put(DatePrecision.DAY, Calendar.DATE);
        calendarCodeByPrecision.put(DatePrecision.HOUR, Calendar.HOUR);
        calendarCodeByPrecision.put(DatePrecision.MINUTE, Calendar.MINUTE);
        calendarCodeByPrecision.put(DatePrecision.SECOND, Calendar.SECOND);
    }

    public boolean startTimer(NodeRef timerRef) {
        return startTimer(timerRef, getCurrentDate(timerRef), 0);
    }

    private boolean startTimer(NodeRef timerRef, Date fromDate, int repeatCounter) {

        String computed = evaluateExpression(timerRef, fromDate, repeatCounter);
        Date occurDate = getNextOccurDate(computed, fromDate, repeatCounter);

        if (occurDate != null) {
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE, occurDate);
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER, repeatCounter);
            nodeService.setProperty(timerRef, CaseTimerModel.PROP_COMPUTED_EXPRESSION, computed);
            return true;
        }
        return false;
    }

    public void stopTimer(NodeRef timerRef) {
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE, null);
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER, 0);
        nodeService.setProperty(timerRef, CaseTimerModel.PROP_COMPUTED_EXPRESSION, null);
    }

    public void timerOccur(NodeRef timerRef) {

        if (caseActivityService.isActive(timerRef)) {

            Date fromDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
            int counter = getRepeatCounter(timerRef) + 1;

            if (!startTimer(timerRef, fromDate, counter)) {
                caseActivityService.stopActivity(timerRef);
            } else {
                eventService.fireEvent(timerRef, ICaseEventModel.CONSTR_ACTIVITY_STOPPED);
            }
        }
    }

    public int getRepeatCounter(NodeRef timerRef) {
        Integer counter = (Integer) nodeService.getProperty(timerRef, CaseTimerModel.PROP_REPEAT_COUNTER);
        return counter != null ? Math.max(0, counter) : 0;
    }

    public boolean isTimerValid(NodeRef timerRef) {
        Date fromDate = getCurrentDate(timerRef);
        String expressionResult = evaluateExpression(timerRef, fromDate, 0);
        Date occurDate = getNextOccurDate(expressionResult, fromDate, 0);
        return occurDate != null;
    }

    public boolean isActive(NodeRef timerRef) {
        Date occurDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
        return occurDate != null;
    }

    public boolean isOccurred(NodeRef timerRef) {
        Date occurDate = (Date) nodeService.getProperty(timerRef, CaseTimerModel.PROP_OCCUR_DATE);
        return occurDate != null && occurDate.getTime() <= System.currentTimeMillis();
    }

    private Date getNextOccurDate(String dateExpression, Date fromDate, int repeatCounter) {

        if (dateExpression != null) {

            int repeatCount = 1;
            String computed = dateExpression;

            Matcher matcher = REPEAT_PATTERN.matcher(computed);
            if (matcher.matches()) {
                computed = matcher.group(2);
                String count = matcher.group(1);
                repeatCount = count.isEmpty() ? -1 : Integer.parseInt(count);
            }

            if (repeatCount == -1 || repeatCounter < repeatCount) {

                char c = computed.charAt(0);

                if (c == 'P' || c == 'p') {
                    String fromDateISO = ISO8601DateFormat.format(fromDate);
                    Interval interval = Interval.parse(String.format("%s/%s", fromDateISO, computed));
                    return interval.getEnd().toDate();
                } else {
                    return ISO8601DateFormat.parse(computed);
                }
            }
        }
        return null;
    }

    private String evaluateExpression(NodeRef timerRef, Date fromDate, int repeatCounter) {
        ExpressionType type = getExpressionType(timerRef);
        Evaluator evaluator = evaluators.get(type);
        if (evaluator == null) {
            throw new IllegalStateException("Expression type " + type + " is not registered!");
        }
        try {
            return evaluator.evaluate(timerRef, fromDate, repeatCounter);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Timer evaluation failed. TimeRef: " + timerRef, e);
        }
    }

    private Date getCurrentDate(NodeRef timerRef) {
        DatePrecision precision = getDatePrecision(timerRef);
        return DateUtils.truncate(new Date(), calendarCodeByPrecision.get(precision));
    }

    private DatePrecision getDatePrecision(NodeRef timerRef) {
        String precision = (String) nodeService.getProperty(timerRef, CaseTimerModel.PROP_DATE_PRECISION);
        return precision != null ? DatePrecision.valueOf(precision) : DatePrecision.DAY;
    }

    private ExpressionType getExpressionType(NodeRef timerRef) {
        String type = (String) nodeService.getProperty(timerRef, CaseTimerModel.PROP_EXPRESSION_TYPE);
        return type != null ? ExpressionType.valueOf(type) : ExpressionType.EXPRESSION;
    }

    public void registerEvaluator(CaseTimerModel.ExpressionType type, Evaluator evaluator) {
        evaluators.put(type, evaluator);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        caseActivityService = (CaseActivityService) serviceRegistry.getService(CiteckServices.CASE_ACTIVITY_SERVICE);
        eventService = (EventService) serviceRegistry.getService(EcosCoreServices.EVENT_SERVICE);
    }
}

package ru.citeck.ecos.icase.timer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.icase.activity.dto.ActivityInstance;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.icase.timer.evaluator.Evaluator;
import ru.citeck.ecos.model.CaseTimerModel;
import ru.citeck.ecos.records2.RecordRef;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CaseTimerEvaluatorService {

    private static final Pattern REPEAT_PATTERN = Pattern.compile("^R([0-9]*)/(.*)");

    private NodeService nodeService;

    private Map<CaseTimerModel.ExpressionType, Evaluator> evaluators = new HashMap<>();
    private Map<CaseTimerModel.DatePrecision, Integer> calendarCodeByPrecision = new HashMap<>();

    @Autowired
    public CaseTimerEvaluatorService(NodeService nodeService) {
        this.nodeService = nodeService;

        this.calendarCodeByPrecision.put(CaseTimerModel.DatePrecision.MONTH, Calendar.MONTH);
        this.calendarCodeByPrecision.put(CaseTimerModel.DatePrecision.DAY, Calendar.DATE);
        this.calendarCodeByPrecision.put(CaseTimerModel.DatePrecision.HOUR, Calendar.HOUR);
        this.calendarCodeByPrecision.put(CaseTimerModel.DatePrecision.MINUTE, Calendar.MINUTE);
        this.calendarCodeByPrecision.put(CaseTimerModel.DatePrecision.SECOND, Calendar.SECOND);
    }


    public String evaluateExpression(NodeRef timerRef, Date fromDate, int repeatCounter) {
        CaseTimerModel.ExpressionType type = getExpressionType(timerRef);
        Evaluator evaluator = getEvaluator(type);
        try {
            return evaluator.evaluate(timerRef, fromDate, repeatCounter);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Timer evaluation failed. TimeRef: " + timerRef, e);
        }
    }

    public String evaluateExpression(RecordRef caseRef, ActivityInstance instance, Date fromDate, int repeatCounter) {
        CaseTimerModel.ExpressionType type = getExpressionType(instance);
        Evaluator evaluator = getEvaluator(type);
        try {
            return evaluator.evaluate(caseRef, instance, fromDate, repeatCounter);
        } catch (Exception e) {
            throw new AlfrescoRuntimeException("Timer evaluation failed. Instance: " + instance, e);
        }
    }

    public Date getNextOccurDate(String dateExpression, Date fromDate, int repeatCounter) {
        return getNextOccurDateImpl(dateExpression, fromDate, repeatCounter);
    }

    public Date getFromDate(NodeRef timerRef) {
        CaseTimerModel.DatePrecision precision = getDatePrecision(timerRef);
        return DateUtils.truncate(new Date(), calendarCodeByPrecision.get(precision));
    }

    public Date getFromDate(ActivityInstance instance) {
        CaseTimerModel.DatePrecision precision = getDatePrecision(instance);
        return DateUtils.truncate(new Date(), calendarCodeByPrecision.get(precision));
    }

    public void registerEvaluator(CaseTimerModel.ExpressionType type, Evaluator evaluator) {
        evaluators.put(type, evaluator);
    }


    private CaseTimerModel.ExpressionType getExpressionType(NodeRef timerRef) {
        String type = (String) nodeService.getProperty(timerRef, CaseTimerModel.PROP_EXPRESSION_TYPE);
        return type != null ? CaseTimerModel.ExpressionType.valueOf(type) : CaseTimerModel.ExpressionType.EXPRESSION;
    }

    private CaseTimerModel.ExpressionType getExpressionType(ActivityInstance instance) {
        String type = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.EXPRESSION_TYPE);
        return type != null ? CaseTimerModel.ExpressionType.valueOf(type) : CaseTimerModel.ExpressionType.EXPRESSION;
    }

    private Evaluator getEvaluator(CaseTimerModel.ExpressionType type) {
        Evaluator evaluator = evaluators.get(type);
        if (evaluator == null) {
            throw new IllegalStateException("Expression type " + type + " is not registered!");
        }
        return evaluator;
    }

    private Date getNextOccurDateImpl(String dateExpression, Date fromDate, int repeatCounter) {
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

    private CaseTimerModel.DatePrecision getDatePrecision(NodeRef timerRef) {
        String precision = (String) nodeService.getProperty(timerRef, CaseTimerModel.PROP_DATE_PRECISION);
        return precision != null ? CaseTimerModel.DatePrecision.valueOf(precision) : CaseTimerModel.DatePrecision.DAY;
    }

    private CaseTimerModel.DatePrecision getDatePrecision(ActivityInstance instance) {
        String precision = EProcUtils.getAnyAttribute(instance, CmmnDefinitionConstants.DATE_PRECISION);
        return precision != null ? CaseTimerModel.DatePrecision.valueOf(precision) : CaseTimerModel.DatePrecision.DAY;
    }

}

package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Pavel Simonov
 */
public final class CaseTimerModel {

    public enum DatePrecision { MONTH, DAY, HOUR, MINUTE, SECOND }
    public enum ExpressionType { EXPRESSION, CRON, SCRIPT }

    public static final String NAMESPACE = "http://www.citeck.ru/model/case/timer/1.0";

    public static final QName TYPE_TIMER = QName.createQName(NAMESPACE, "timer");

    public static final QName PROP_TIMER_EXPRESSION = QName.createQName(NAMESPACE, "timerExpression");
    public static final QName PROP_COMPUTED_EXPRESSION = QName.createQName(NAMESPACE, "computedExpression");
    public static final QName PROP_OCCUR_DATE = QName.createQName(NAMESPACE, "occurDate");
    public static final QName PROP_REPEAT_COUNTER = QName.createQName(NAMESPACE, "repeatCounter");
    public static final QName PROP_WORKING_DAYS = QName.createQName(NAMESPACE, "workingDays");
    public static final QName PROP_DATE_PRECISION = QName.createQName(NAMESPACE, "datePrecision");
    public static final QName PROP_EXPRESSION_TYPE = QName.createQName(NAMESPACE, "expressionType");
}

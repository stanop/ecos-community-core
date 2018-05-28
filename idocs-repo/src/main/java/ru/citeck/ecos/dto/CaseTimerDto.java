package ru.citeck.ecos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Case timer data transfer object
 */
public class CaseTimerDto extends CaseModelDto {

    /**
     * Data transfer object type
     */
    public static final String DTO_TYPE = "caseTimer";

    /**
     * Expression type
     */
    private String expressionType;

    /**
     * Timer expression
     */
    private String timerExpression;

    /**
     * Date precision
     */
    private String datePrecision;

    /**
     * Computed expression
     */
    private String computedExpression;

    /**
     * Occur date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occurDate;

    /**
     * Repeat counter
     */
    private Integer repeatCounter;

    /** Getters and setters */

    public String getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    public String getTimerExpression() {
        return timerExpression;
    }

    public void setTimerExpression(String timerExpression) {
        this.timerExpression = timerExpression;
    }

    public String getDatePrecision() {
        return datePrecision;
    }

    public void setDatePrecision(String datePrecision) {
        this.datePrecision = datePrecision;
    }

    public String getComputedExpression() {
        return computedExpression;
    }

    public void setComputedExpression(String computedExpression) {
        this.computedExpression = computedExpression;
    }

    public Date getOccurDate() {
        return occurDate;
    }

    public void setOccurDate(Date occurDate) {
        this.occurDate = occurDate;
    }

    public Integer getRepeatCounter() {
        return repeatCounter;
    }

    public void setRepeatCounter(Integer repeatCounter) {
        this.repeatCounter = repeatCounter;
    }


}

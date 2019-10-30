package ru.citeck.ecos.flowable.listeners;

import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import ru.citeck.ecos.calendar.eform.EcosCalendarEntry;
import ru.citeck.ecos.calendar.eform.EcosCalendarEntryDTO;
import ru.citeck.ecos.calendar.service.EcosCalendarService;
import ru.citeck.ecos.flowable.example.AbstractExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.utils.JsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class SetCalaendarEntryExecutionListener extends AbstractExecutionListener {

    private EcosCalendarService ecosCalendarService;
    private JsUtils jsUtils;

    private Expression calendarId;
    private Expression title;
    private Expression description;
    private Expression isAllDay;
    private Expression start;
    private Expression end;
    private Expression participants;
    private Expression transparency;
    private Expression eventName;
    private Expression eventNameVar;

    @Override
    protected void initImpl() {
        this.ecosCalendarService = ApplicationContextProvider.getBean(EcosCalendarService.class);
        this.jsUtils = ApplicationContextProvider.getBean(JsUtils.class);
    }

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        if (ecosCalendarService == null) {
            throw new IllegalStateException("EcosCalendarService implementation not found");
        }

        String calendarIdValue = (String) getExpresionValueOrNull(calendarId, execution);
        if (StringUtils.isBlank(calendarIdValue)) {
            throw new IllegalArgumentException("Calendar id could not be empty");
        }

        String titleValue = (String) getExpresionValueOrNull(title, execution);
        String descriptionValue = (String) getExpresionValueOrNull(description, execution);
        String eventNameValue = (String) getExpresionValueOrNull(eventName, execution);
        String transparencyValue = (String) getExpresionValueOrNull(transparency, execution);
        Date startValue = parseDate(getExpresionValueOrNull(start, execution), execution);
        Date endValue = parseDate(getExpresionValueOrNull(end, execution), execution);
        ArrayList<String> participantsValue = parseParticipants(getExpresionValueOrNull(participants, execution));

        EcosCalendarEntry dto = null;
        if (eventNameValue != null) {
            dto = ecosCalendarService.getCalendarEntry(calendarIdValue, eventNameValue);
        }

        if (dto == null) {
            EcosCalendarEntryDTO newDto = new EcosCalendarEntryDTO();
            newDto.setTitle(titleValue);
            newDto.setDescription(descriptionValue);
            newDto.setStart(startValue);
            newDto.setEnd(endValue);
            newDto.setParticipants(participantsValue);
            newDto.setTransparency(transparencyValue);

            dto = ecosCalendarService.createCalendarEntry(calendarIdValue, newDto);

            String eventNameVarValue = (String) getExpresionValueOrNull(eventNameVar, execution);
            if (!StringUtils.isBlank(eventNameVarValue)) {
                execution.setVariable(eventNameVarValue, dto.getSystemName());
            }
        } else {
            dto.setTitle(titleValue);
            dto.setDescription(descriptionValue);
            dto.setStart(startValue);
            dto.setEnd(endValue);
            dto.setParticipants(participantsValue);
            dto.setTransparency(transparencyValue);
            ecosCalendarService.updateCalendarEntry(dto, calendarIdValue);
        }
    }

    private Object getExpresionValueOrNull(Expression expression, DelegateExecution execution) {
        return expression != null ? expression.getValue(execution) : null;
    }

    private Date parseDate(Object dateValue, DelegateExecution execution) {
        if (dateValue == null) {
            return null;
        }

        Object javaObj = jsUtils.toJava(dateValue);

        boolean isAllDayValue = FlowableListenerUtils.getBooleanFromExpressionOrDefault(isAllDay, execution, true);

        if (javaObj instanceof Date) {
            String iso8601 = ISO8601DateFormat.format((Date) javaObj);
            return EcosCalendarEntryDTO.extractDate(isAllDayValue, iso8601);
        }

        if (javaObj instanceof String) {
            return EcosCalendarEntryDTO.extractDate(isAllDayValue, (String) javaObj);
        }

        throw new IllegalArgumentException("Unsupported date format");
    }

    private ArrayList<String> parseParticipants(Object participantsValue) {
        if (participantsValue == null) {
            return new ArrayList<>();
        }

        Object javaObj = jsUtils.toJava(participantsValue);

        if (javaObj instanceof Collection) {
            Collection<?> items = (Collection<?>) javaObj;
            ArrayList<String> participants = new ArrayList<>(items.size());
            for (Object item : items) {
                participants.add(String.valueOf(item));
            }
            return participants;
        }

        if (javaObj instanceof String) {
            String str = (String) javaObj;
            return new ArrayList<>(Arrays.asList(str.split("\\s*,\\s*")));
        }

        throw new IllegalArgumentException("Unsupported participants format");
    }
}
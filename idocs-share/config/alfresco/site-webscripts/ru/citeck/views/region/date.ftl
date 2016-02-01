<#assign labels     = { "month" :  msg("date-unit.single.month"), "year" : msg("date-unit.single.year"), "header": msg("date.select") }>
<#assign buttons    = { "submit" :  msg("button.ok"), "cancel" : msg("button.cancel") }>
<#assign format     = msg("date.format")>
<#assign formatIE     = msg("date.formatIE")>
<#assign months     = msg("months.short")>
<#assign days       = msg("days.short")>

<div id="${fieldId}-dateControl" data-bind='dateControl: textValue, 
    localization: {
        format: "${format}",
        formatIE: "${formatIE}",
        labels: { month: "${labels.month}", year: "${labels.year}", header: "${labels.header}" },
        buttons: { submit: "${buttons.submit}", cancel: "${buttons.cancel}" },
        months: "${months}",
        days: "${days}"
    }'>
    <input id="${fieldId}" type="date" data-bind="value: textValue, disable: protected" />
    <a id="${fieldId}-calendarAccessor" class="calendar-link-button hidden">
        <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">
    </a>    
</div>
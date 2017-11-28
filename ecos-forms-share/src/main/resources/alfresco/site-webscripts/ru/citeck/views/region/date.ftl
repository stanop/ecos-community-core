<#assign params = viewScope.region.params!{} />
<#assign mode   = params.mode!"browser" />

<#assign labels     = { "month" :  msg("date-unit.single.month"), "year" : msg("date-unit.single.year"), "header": msg("date.select") }>
<#assign buttons    = { "submit" :  msg("button.ok"), "cancel" : msg("button.cancel") }>
<#assign placeholder>
    <#if config.scoped["DateFormatMask"]?? && config.scoped["DateFormatMask"]["placeholder"]?? && config.scoped["DateFormatMask"]["placeholder"].value>
        ${config.scoped["DateFormatMask"]["placeholder"].value}
    <#else>
        ${msg("date.formatIE")}
    </#if>
</#assign>
<#assign months     = msg("months.short")>
<#assign days       = msg("days.short")>

<div id="${fieldId}-dateControl" data-bind='dateControl: value,
    mode: "${mode}",
    localization: {
        labels: { month: "${labels.month}", year: "${labels.year}", header: "${labels.header}" },
        buttons: { submit: "${buttons.submit}", cancel: "${buttons.cancel}" },
        months: "${months}",
        days: "${days}"
    }'>

    <input id="${fieldId}" type="date" placeholder="${placeholder?trim}" data-bind="value: ko.computed({
        read: function() {
            var result = value();
            if (result instanceof Date) {
                result = Alfresco.util.formatDate(result, 'yyyy-mm-dd');
            }
            return result;
        },
        write: function(newValue) {
            value(newValue);
        }
    }), disable: protected">
    
    <!-- ko ifnot: protected -->
        <a id="${fieldId}-calendarAccessor" class="calendar-link-button hidden">
            <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">
        </a>
    <!-- /ko -->

    <!-- ko if: protected -->
        <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">
    <!-- /ko -->
</div>
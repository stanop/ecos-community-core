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

    <#if config.scoped["DateFormatMask"]?? && config.scoped["DateFormatMask"]["mask"]??>
        <#assign mask = config.scoped["DateFormatMask"]["mask"].value?trim!"DD.MM.YYYY">

        <input id="${fieldId}" type="text" placeholder="${placeholder?trim}" data-bind="value: ko.computed({
            read: function() {
                return value() ? moment(value()).format('${mask}') : null;
            },
            write: function(newValue) {
                var dateWrapper = $root.rootObjects().moment(newValue, '${mask}');
                value(dateWrapper.isValid() ? dateWrapper.toDate() : null)
            }
        }), disable: protected"> 
    <#else>
        <#assign type><#if mode == "browser">date<#else>text</#if></#assign>
        
        <input id="${fieldId}" placeholder="${placeholder?trim}" type="${type}"
               data-bind="value: textValue, disable: protected" 
        />
    </#if>
    
    <!-- ko ifnot: protected -->
        <a id="${fieldId}-calendarAccessor" class="calendar-link-button hidden">
            <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">
        </a>
    <!-- /ko -->

    <!-- ko if: protected -->
        <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">
    <!-- /ko -->
</div>
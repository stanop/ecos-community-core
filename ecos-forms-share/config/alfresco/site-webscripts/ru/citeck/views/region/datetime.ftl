<#assign labels   = { "month" :  msg("date-unit.single.month"), "year" : msg("date-unit.single.year"), "header": msg("date.select") }>
<#assign buttons  = { "submit" :  msg("button.ok"), "cancel" : msg("button.cancel") }>
<#assign format   = msg("datetime.format")>
<#assign formatIE = msg("datetime.formatIE")>
<#assign months   = msg("months.short")>
<#assign days     = msg("days.short")>
<#assign params = viewScope.region.params!{} />
<#assign mode   = params.mode!"browser" />

<div id="${fieldId}-datetime-control" class="datetime-control" 
    data-bind="component: { name: 'datetime', params: {
        fieldId: $element.id,
        protected: protected,
        value: value,
        mode: $data.mode || '${mode}',
        localization: {
            format: '${format}',
            formatIE: '${formatIE}',
            labels: { month: '${labels.month}', year: '${labels.year}', header: '${labels.header}' },
            buttons: { submit: '${buttons.submit}', cancel: '${buttons.cancel}' },
            months: '${months}',
            days: '${days}'
        }
    }}">
</div>
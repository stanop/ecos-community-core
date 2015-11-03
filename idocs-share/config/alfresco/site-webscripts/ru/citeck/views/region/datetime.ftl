<#assign labels   = { "month" :  msg("date-unit.single.month"), "year" : msg("date-unit.single.year"), "header": msg("date.select") }>
<#assign buttons  = { "submit" :  msg("button.ok"), "cancel" : msg("button.cancel") }>
<#assign format   = msg("date.format")>
<#assign months   = msg("months.short")>
<#assign days     = msg("days.short")>

<div id="${fieldId}-datetime-control" class="datetime-control" 
    data-bind="component: { name: 'datetime', params: {
        fieldId: $element.id,
        protected: protected,
        value: value, 
        localization: {
            format: '${format}',
            labels: { month: '${labels.month}', year: '${labels.year}', header: '${labels.header}' },
            buttons: { submit: '${buttons.submit}', cancel: '${buttons.cancel}' },
            months: '${months}',
            days: '${days}'
        }
    }}">
</div>
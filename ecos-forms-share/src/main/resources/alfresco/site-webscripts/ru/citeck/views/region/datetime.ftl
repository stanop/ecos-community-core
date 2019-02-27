<#assign params     = viewScope.region.params!{} />
<#assign labels     = { "month" :  msg("date-unit.single.month"), "year" : msg("date-unit.single.year"), "header": msg("date.select") }>
<#assign buttons    = { "submit" :  msg("button.ok"), "cancel" : msg("button.cancel") }>
<#assign placeholderFormat   = params.placeholderFormat!msg("datetime.format")>
<#assign placeholderFormatIE = params.placeholderFormatIE!msg("datetime.formatIE")>
<#assign months     = msg("months.short")>
<#assign days       = msg("days.short")>
<#assign mode       = params.mode!"browser" />
<#assign dateFormat = params.dateFormat!"YYYY-MM-DD HH:mm:ss" />

<div id="${fieldId}-datetime-control" class="datetime-control" 
    data-bind="component: { name: 'datetime', params: {
        fieldId: $element.id,
        protected: protected,
        value: value,
        mode: $data.mode && $data.mode() || '${mode}',
        dateFormat: '${dateFormat}',
        localization: {
            placeholderFormat: '${placeholderFormat}',
            placeholderFormatIE: '${placeholderFormat}',
            labels: { month: '${labels.month}', year: '${labels.year}', header: '${labels.header}' },
            buttons: { submit: '${buttons.submit}', cancel: '${buttons.cancel}' },
            months: '${months}',
            days: '${days}'
        }
    }}">
</div>
<#assign params = viewScope.region.params!{} />
<#assign title = params.title!msg("button.add") />

<div id="${fieldId}-multiple-text-control" class="multiple-text-control" data-bind='component: { name: "multiple-text", params: {
    disabled: protected,
    value: value,
    title: "${title}"
}}'>
</div>
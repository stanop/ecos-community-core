<#assign params = viewScope.region.params!{} />
<#assign mode = params.mode!"button" />

<!-- ko component: { name: "simple-checkbox", params: {
    id: "${fieldId}",
    label: "${msg('button.generate')}",
    mode: "${mode}"
}} --><!-- /ko -->
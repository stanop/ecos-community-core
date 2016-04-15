<#assign params = viewScope.region.params!{} />
<#assign mode = params.mode!"button" />

<!-- ko component: { name: "number-generate", params: {
    id: "${fieldId}",
    enumeration: enumeration,
    template: "${params.template}",
    node: node,
    value: value,
    label: "${msg('button.generate')}",       
    disable: protected,
    mode: "${mode}"
}} --><!-- /ko -->
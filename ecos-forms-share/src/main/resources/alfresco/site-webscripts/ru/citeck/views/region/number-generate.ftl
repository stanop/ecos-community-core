<#assign params = viewScope.region.params!{} />
<#assign mode = params.mode!"button" />
<#if params.template?contains("()")>
   <#assign numberTemplate = params.template />
<#else>
   <#assign numberTemplate = "\"" + params.template + "\"" />
</#if>
<#if params.flagOn?? >
   <#assign flagOn = params.flagOn>
</#if>
<#if params.generateOff?? >
   <#assign generateOff = params.generateOff>
</#if>

<!-- ko component: { name: "number-generate", params: {
    id: "${fieldId}",
    enumeration: enumeration,
    template: ${numberTemplate},
    node: node,
    value: value,
    label: "${msg('button.generate')}",
    disable: protected,
    mode: "${mode}",
    <#if params.flagOn??>
    flagOn: "${flagOn}",
    </#if>
    <#if params.generateOff??>
    generateOff: "${generateOff}",
    </#if>
}} --><!-- /ko -->
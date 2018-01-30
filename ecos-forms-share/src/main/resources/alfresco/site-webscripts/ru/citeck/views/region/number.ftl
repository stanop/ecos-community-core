<#assign params = viewScope.region.params>

<!-- ko component: { name: "number", params: {
  <#if params.step??>
    step: ${params.step},
  </#if>
  <#if params.isInteger??>
    isInteger: ${params.isInteger},
  </#if>
  id: "${fieldId}",
  value: textValue,
  disable: protected
}} --><!-- /ko -->
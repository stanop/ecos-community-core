<#assign params = viewScope.region.params>

<!-- ko component: { name: "number", params: {
  <#if params.step??>
    step: ${params.step},
  </#if>

  id: "${fieldId}",
  value: textValue,
  disable: protected
}} --><!-- /ko -->
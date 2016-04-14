<#assign params = viewScope.region.params>

<!-- ko component: { name: "number", params: {
  <#if params.step??>
    step: ${params.step},
  </#if>
  <#if params.onlyNumbers??>
    onlyNumbers: ${params.onlyNumbers},
  </#if>

  id: "${fieldId}",
  value: textValue,
  disable: protected
}} --><!-- /ko -->
<#assign params = viewScope.region.params!{} />

<#if params.text?? || params.key??>
  <label for="${fieldId}">
    <#if params.text??>${params.text?html}</#if>
    <#if params.key??>${msg(params.key?js_string)}</#if>
  </label>
<#else>
  <#assign feature = params.feature!"title" />
  <label for="${fieldId}" data-bind="text: $data['${feature}']() || name()"></label>
</#if>
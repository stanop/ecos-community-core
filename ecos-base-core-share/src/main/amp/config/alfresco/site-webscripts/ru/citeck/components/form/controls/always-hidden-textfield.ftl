<#if field.control.params.value??>
	<#assign v = field.control.params.value>
<#else>
	<#assign v = field.value>
</#if>
<#if v?is_number>
	<#assign fieldValue = v?c />
<#elseif v?is_boolean />
	<#assign fieldValue = v?string />
<#else>
	<#assign fieldValue = v?html />
</#if>
<input id="${fieldHtmlId}" name="${field.name}" tabindex="0" type="hidden" value="${fieldValue}"/>

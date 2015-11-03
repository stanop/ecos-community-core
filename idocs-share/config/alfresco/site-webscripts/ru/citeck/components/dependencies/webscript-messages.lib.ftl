<#macro renderMessagesJSON msgs>
{
<#list msgs?keys as key>
	"${key?js_string}": "${msgs[key]?js_string}"<#if key_has_next>,</#if>
</#list>
}
</#macro>

<#macro renderMessages msgs>
<#list msgs?keys as key>
${key?js_string}=${msgs[key]?j_string}
</#list>
</#macro>


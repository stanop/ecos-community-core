<#escape x as jsonUtils.encodeJSONString(x)>
{
	"found": ${found?string}<#if found>,
	"nodeRef": "${nodeRef}"</#if>
}
</#escape>

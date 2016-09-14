<#escape x as jsonUtils.encodeJSONString(x)>
{
	"type": "${type}",
	"datatype": "${datatype}",
	"nodetype": <#if nodetype??>"${nodetype}"<#else>null</#if>
}
</#escape>
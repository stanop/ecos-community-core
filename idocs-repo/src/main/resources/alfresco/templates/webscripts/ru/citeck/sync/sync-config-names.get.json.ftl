<#escape x as jsonUtils.encodeJSONString(x)>
{
	"configs": [
	<#list configs as cfg>
		{
			"name": "${cfg}"
		}<#if cfg_has_next>,</#if>
	</#list>
	]
}
</#escape>
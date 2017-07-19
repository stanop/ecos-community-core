<#import "view-attributes.lib.ftl" as wa />
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#list views?keys as key>
		"${key}": [ <@wa.renderElement views[key] /> ]<#if key_has_next>,</#if>
	</#list>
}
</#escape>

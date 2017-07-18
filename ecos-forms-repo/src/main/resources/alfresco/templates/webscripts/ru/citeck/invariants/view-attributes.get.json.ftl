<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#list views?keys as key>
		"${key}": [ <@renderElement views[key] /> ]<#if key_has_next>,</#if>
	</#list>
}
</#escape>

<#macro renderElement element>
	<#if element.getClass??>
		<#assign elementClass = element.getClass().toString() />
	<#else/>
		<#assign elementClass = "" />
	</#if>

	<#if elementClass == "class ru.citeck.ecos.invariants.view.NodeView">
		<@renderView element />
	<#elseif elementClass == "class ru.citeck.ecos.invariants.view.NodeField">
		<@renderField element />
	</#if>
</#macro>

<#macro renderView view><#escape x as jsonUtils.encodeJSONString(x)>
	<#list view.elements as element>
		<@renderElement element /><#if element_has_next>,</#if>
	</#list>
</#escape></#macro>

<#macro renderField field>
<#escape x as jsonUtils.encodeJSONString(x)>
	<#if field.attributeName??>"${shortQName(field.attributeName)}"<#else>null</#if>
</#escape></#macro>

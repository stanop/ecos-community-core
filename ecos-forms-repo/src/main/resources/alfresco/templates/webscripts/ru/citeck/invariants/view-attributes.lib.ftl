<#macro renderElement element>
	<#if !element?is_enumerable>
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
	</#if>
</#macro>

<#macro renderView view><#escape x as jsonUtils.encodeJSONString(x)>
	<#list view.elements as element>
		<@renderElement element />
	</#list>
</#escape></#macro>

<#macro renderField field><#escape x as jsonUtils.encodeJSONString(x)>
	<#if field.attributeName??>"${shortQName(field.attributeName)}",</#if>
</#escape></#macro>
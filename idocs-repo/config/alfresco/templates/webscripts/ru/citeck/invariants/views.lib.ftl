<#import "invariants.lib.ftl" as inv />

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
	<#elseif elementClass == "class ru.citeck.ecos.invariants.view.NodeViewRegion">
		<@renderRegion element />
	<#else>
		{
			"type": "${valueClass}"
		}
	</#if>
</#macro>

<#macro renderView view>
<#escape x as jsonUtils.encodeJSONString(x)>{
	"type": "view",
	"class": <#if view.className??>"${shortQName(view.className)}"<#else>null</#if>,
	"id": <#if view.id??>"${view.id}"<#else>null</#if>,
	"mode": <#if view.mode??>"${view.mode}"<#else>null</#if>,
	"kind": <#if view.kind??>"${view.kind}"<#else>null</#if>,
	"template": <#if view.template??>"${view.template}"<#else>null</#if>,
	"params": {
		<#list view.params?keys as key>
		"${key}": <#if view.params[key]??>"${view.params[key]}"<#else>null</#if><#if key_has_next>,</#if>
		</#list>
	},
	"elements": [
		<#list view.elements as element>
<@renderElement element /><#if element_has_next>,</#if>
		</#list>
	]
}</#escape></#macro>

<#macro renderField field>
<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"type": "field",
		"attribute": <#if field.attributeName??>"${shortQName(field.attributeName)}"<#else>null</#if>,
		"id": <#if field.id??>"${field.id}"<#else>null</#if>,
		"kind": <#if field.kind??>"${field.kind}"<#else>null</#if>,
		"datatype": <#if field.datatypeName??>"${shortQName(field.datatypeName)}"<#else>null</#if>,
		"nodetype": <#if field.nodetypeName??>"${shortQName(field.nodetypeName)}"<#else>null</#if>,
		"template": <#if field.template??>"${field.template}"<#else>null</#if>,
		"params": {
			<#list field.params?keys as key>
			"${key}": <#if field.params[key]??>"${field.params[key]}"<#else>null</#if><#if key_has_next>,</#if>
			</#list>
		},
		"regions": [
			<#list field.regions as region>
<@renderRegion region /><#if region_has_next>,</#if>
			</#list>
		],
		"invariants": [
			<#list field.invariants as invariant>
<@inv.renderInvariant invariant /><#if invariant_has_next>,</#if>
			</#list>
		]
	}</#escape></#macro>

<#macro renderRegion region>
<#escape x as jsonUtils.encodeJSONString(x)>
		{
			"type": "region",
			"name": "${region.name}",
			"id": <#if region.id??>"${region.id}"<#else>null</#if>,
			"kind": <#if region.kind??>"${region.kind}"<#else>null</#if>,
			"template": <#if region.template??>"${region.template}"<#else>null</#if>,
			"params": {
				<#list region.params?keys as key>
				"${key}": <#if region.params[key]??>"${region.params[key]}"<#else>null</#if><#if key_has_next>,</#if>
				</#list>
			}
		}</#escape></#macro>

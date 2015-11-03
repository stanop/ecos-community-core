<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#if (props?size > 0) >
	"props": {
		<#-- include nodeRef in result, id of this string will be contain value of includeNodeRef -->
		<#if args.includeNodeRef??>"nodeRef": "${args.nodeRef}",</#if>
		<#list props as line>
			<#if args.replaceColon??>
				<#assign prop2=line.key?replace(":", args.replaceColon)>
			<#else> <#assign prop2=line.key> </#if>
			"${prop2}": <#if line.value??><@printValue line.value null/><#else>null</#if><#if line_has_next>,</#if>
		</#list>
	}
	</#if>
	<#if (assocs?size > 0) >
		<#if (props?size > 0) >,</#if>
	"assocs": {
		<#list assocs as line>
			<#if args.replaceColon??>
				<#assign prop2=line.key?replace(":", args.replaceColon)>
			<#else> <#assign prop2=line.key> </#if>
			"${prop2}": [
				<#list line.value as ln>
					"${ln}"<#if ln_has_next>,</#if>
				</#list>
			]<#if line_has_next>,</#if>
		</#list>
	}
	</#if>
	<#if (childAssocs?size > 0) >
		<#if (props?size > 0) || (assocs?size > 0) >,</#if>
	"childAssocs": {
		<#list childAssocs as line>
			<#if args.replaceColon??>
				<#assign prop2=line.key?replace(":", args.replaceColon)>
			<#else>  <#assign prop2=line.key>  </#if>
			"${prop2}": [
				<#list line.value as ln>
					"${ln}"<#if ln_has_next>,</#if>
				</#list>
			]<#if line_has_next>,</#if>
		</#list>
	}
	</#if>

	<#if siteShortName?? >
	<#if (props?size > 0) || (assocs?size > 0) || (childAssocs?size > 0) >,</#if>
	"siteShortName": "${siteShortName}"
	</#if>

	<#if (args.getParent??) && (node??) && (node.parent??) >
	<#if (props?size > 0) || (assocs?size > 0) || (childAssocs?size > 0) || siteShortName?? >,</#if>
	"parent": "${node.parent.nodeRef}"
	</#if>
}
</#escape>

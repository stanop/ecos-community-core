<#macro renderInvariant invariant><#escape x as jsonUtils.encodeJSONString(x)>{
	"scope": {
		"class": <#if invariant.classScope??>"${shortQName(invariant.classScope)}"<#else>null</#if>,
		"classKind": <#if invariant.classScopeKind??>"${invariant.classScopeKind?string?lower_case}"<#else>null</#if>,
		"attribute": <#if invariant.attributeScope??>"${shortQName(invariant.attributeScope)}"<#else>null</#if>,
		"attributeKind": <#if invariant.attributeScopeKind??>"${invariant.attributeScopeKind?string}"<#else>null</#if>
	},
	"feature": "${invariant.feature?string}",
	"priority": "${invariant.priority?string}",
	"description": "${invariant.description!}",
	"final": ${invariant.final?string},
	"language": "${invariant.language}",
	"expression": 
		<#assign value = invariant.value />
		<#if value?is_hash>
			<#if value.getClass??>
				<#assign valueClass = value.getClass().toString() />
			<#else/>
				<#assign valueClass = "" />
			</#if>
			<#if valueClass == "class ru.citeck.ecos.search.SearchCriteria">
			[
			<#list value.triplets as criterion>
				{
					"attribute": "${criterion.field}",
					"predicate": "${criterion.predicate}",
					"value": <#if criterion.value??>"${criterion.value}"<#else>null</#if>
				}<#if criterion_has_next>,</#if>
			</#list>
			]
			<#else>
			{
				"class": "${valueClass}"
			}
			</#if>
		<#elseif value?is_string>
			"${invariant.value}"
		<#elseif value?is_sequence>
			[
			<#list value as item>
				"${item}"<#if item_has_next>,</#if>
			</#list>
			]
		<#else>
			null
		</#if>
}</#escape></#macro>

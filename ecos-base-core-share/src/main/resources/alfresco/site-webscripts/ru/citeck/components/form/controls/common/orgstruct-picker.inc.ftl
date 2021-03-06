<#include "dynamic-tree-picker.inc.ftl" />

<#macro renderOrgstructModelJS params = {}>
	<#assign 
		valueField = (params.valueField!"nodeRef")
		rootGroup = (params.rootGroup!"_orgstruct_home_")
		isDefaultRoot = (params.isDefaultRoot!"false")
		childrenQuery = (params.childrenQuery!"")
		searchRoot = (params.searchRoot!rootGroup)
		searchQuery = (params.searchQuery!childrenQuery)
		rootUrl = (params.rootUrl!"")
	/>
<#assign excludeAuthorities>
    <#if config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]?? && params.excludeAuthorities??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"] + "," + params.excludeAuthorities}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]}
    <#elseif params.excludeAuthorities??>
         ${params.excludeAuthorities}
    <#else></#if>
</#assign>

	{
		formats: {
			"authority": {
				name: "authority-{${valueField}}",
				keys: [ "selected-{selected}", "{groupType}-{roleIsManager}", "{authorityType}-{groupType}", "{authorityType}", "authority" ]
			},
			"selected-items": {
				name: "selected-items",
				keys: [ "selected-items" ],
			},
		},
		item: {
			"": {
				"format": "authority",
				"get": "${url.context}/proxy/alfresco/api/orgstruct/authority/?${valueField}={${valueField}}",
			},
		},
		children: {
			"root": {
				"format": "authority",
				"get": "<#if rootUrl != "" >${rootUrl}<#else>${url.context}/proxy/alfresco/api/orgstruct/group/<#if isDefaultRoot=="true">?root=true<#else>${rootGroup}/children/?${childrenQuery}</#if></#if>",
			},
			"search": {
				"format": "authority",
				"get": "${url.context}/proxy/alfresco/api/orgstruct/<#if searchRoot == "">root<#else>group/${searchRoot}/children</#if>/?filter={query}&recurse=true&${searchQuery}"
			},
			"selected-items": {
				"format": "authority",
			},
			"GROUP": {
				"format": "authority",
				"get": "${url.context}/proxy/alfresco/api/orgstruct/group/{shortName}/children/?${childrenQuery}",
			},
		},
		titles: {
			"root": "{title}",
			"GROUP": "{displayName} ({shortName})",
			"USER": "{firstName} {lastName} ({shortName})",
		},
		excludeAuthorities: "${excludeAuthorities?trim}"
	}
</#macro>
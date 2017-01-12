<#escape x as jsonUtils.encodeJSONString(x)>
{
	"modules": [
	<#list modules as module>
		{
			"id": "${module.id}",
			"title": "${module.title!}",
			"description": "${module.description!}",
			"version": "${module.version!}",
			"installState": "${module.installState!}",
			"installDate": <#if installDate??>"${module.installDate}"<#else>null</#if>,
			"aliases": [
			<#list module.aliases![] as alias>
				"${alias}"<#if alias_has_next>,</#if>
			</#list>
			],
			"dependencies": {
			<#list module.dependencies![] as dependency>
				"${dependency.dependencyId}": "${dependency.versionString}"<#if dependency_has_next>,</#if>
			</#list>
			},
			"editions": [
			<#list module.editions![] as edition>
				"${edition}"<#if edition_has_next>,</#if>
			</#list>
			]
		}<#if module_has_next>,</#if>
	</#list>
	]
}
</#escape>
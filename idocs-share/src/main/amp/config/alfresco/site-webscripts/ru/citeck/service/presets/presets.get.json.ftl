<#escape x as jsonUtils.encodeJSONString(x)>
{
	"type": "${presetType}",
	"presets": [
	<#list presets as preset>
		{
			"id": "${preset.id}",
			"name": "${preset.name!preset.id}"
		}<#if preset_has_next>,</#if>
	</#list>
	]
}
</#escape>
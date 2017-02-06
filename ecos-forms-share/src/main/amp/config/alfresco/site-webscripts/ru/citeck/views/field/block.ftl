<div class="block-region block-region-label">
	<@views.renderRegion "label" />	
	<@views.renderRegion "mandatory" />
</div>

<#if hasRegion("input")>
	<div class="block-region block-region-input">
		<@views.renderRegion "input" />
	</div>
</#if>

<#if hasRegion("select")>
	<div class="block-region block-region-select">
		<@views.renderRegion "select" />
	</div>
</#if>

<@views.renderRegion "help" />
<@views.renderRegion "message" />

<#function hasRegion regionName>
	<#if viewScope.field.regions?has_content>
		<#list viewScope.field.regions as region>
			<#if region.name == regionName><#return true></#if>
		</#list>
		<#return false>
	</#if>
</#function>
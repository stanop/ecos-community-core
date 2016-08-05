<#assign params = viewScope.region.params!{} />
<#assign alwaysSingle = params.alwaysSingle!"false" />

<#assign caption>
	<#if params.caption??>
		${params.caption}
	<#elseif config.scoped["InvariantControlsConfiguration"]?? && config.scoped["InvariantControlsConfiguration"].select??>
		<#if config.scoped["InvariantControlsConfiguration"]["select"].attributes["caption"]??>
			${config.scoped["InvariantControlsConfiguration"]["select"].attributes["caption"]}
		</#if>
	<#else>
		form.select.label
	</#if>
</#assign>

<!-- ko ifnot: multiple -->
<select id="${fieldId}" data-bind="
	options: options, 
	optionsCaption: '${msg(caption?trim)}', 
	optionsText: function(item) { return getValueTitle(item) }, 
	value: value, 
	<#-- use this to suppress initial value set -->
	<#-- and thus support 'default' feature -->
	valueAllowUnset: true, 
	disable: protected">
</select>
<!-- /ko -->

<!-- ko if: multiple -->
<select id="${fieldId}" <#if alwaysSingle != "true">multiple="true"</#if> data-bind="
	options: options, 
	optionsText: function(item) { return getValueTitle(item) }, 
	selectedOptions: value, 
	<#-- use this to suppress initial value set -->
	<#-- and thus support 'default' feature -->
	valueAllowUnset: true, 
	disable: protected">
</select>
<!-- /ko -->
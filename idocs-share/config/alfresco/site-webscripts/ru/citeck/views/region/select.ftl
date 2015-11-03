<#assign params = viewScope.region.params!{} />
<!-- ko ifnot: multiple -->
<select id="${fieldId}" data-bind="
	options: options, 
	optionsCaption: '${msg("form.select.label")}', 
	optionsText: function(item) { return getValueTitle(item) }, 
	value: value, 
	<#-- use this to suppress initial value set -->
	<#-- and thus support 'default' feature -->
	valueAllowUnset: true, 
	disable: protected">
</select>
<!-- /ko -->
<!-- ko if: multiple -->
<select id="${fieldId}" <#if params.alwaysSingle!"false" != "true">multiple="true"</#if> data-bind="
	options: options, 
	optionsText: function(item) { return getValueTitle(item) }, 
	selectedOptions: value, 
	<#-- use this to suppress initial value set -->
	<#-- and thus support 'default' feature -->
	valueAllowUnset: true, 
	disable: protected">
</select>
<!-- /ko -->
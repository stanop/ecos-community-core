<#assign params = viewScope.region.params!{} />
<#assign alwaysSingle = params.alwaysSingle!"false" />

<#assign optionsCaption>
	<#if params.optionsCaption??>
		${params.optionsCaption}
	<#elseif config.scoped["InvariantControlsConfiguration"]?? && config.scoped["InvariantControlsConfiguration"].select??>
		<#if config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsCaption"]??>
			${config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsCaption"]}
		</#if>
	<#else>form.select.label</#if>
</#assign>

<!-- ko component: { name: "select", params: {
	id: "${fieldId}",
	multiple: multiple, disable: protected,
	selectedOptions: multipleValues,

	options: options,  
    optionsCaption: "${msg(optionsCaption?trim)}",
	optionsText: function(option) { return <#if params.optionsText??>${params.optionsText?trim}<#else>getValueTitle(option)</#if>; },
	optionsValue: <#if params.optionsValue??>function(option) { return ${params.optionsValue?trim}; }<#else>null</#if>,
	optionsAfterRender: <#if params.optionsAfterRender??>function(option) { return ${params.optionsAfterRender?trim}; }<#else>null</#if>
}} --><!-- /ko -->
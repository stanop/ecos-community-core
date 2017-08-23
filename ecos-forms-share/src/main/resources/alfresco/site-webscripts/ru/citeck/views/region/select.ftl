<#assign params = viewScope.region.params!{} />

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

	data: $data,
	
	trottle: <#if params.trottle??>${params.trottle}<#else>false</#if>,
	valueAllowUnset: <#if params.valueAllowUnset??>${params.valueAllowUnset}<#else>true</#if>,

    optionsCaption: "${msg(optionsCaption?trim)}",
	optionsText: <#if params.optionsText??>function(option) { return ${params.optionsText?trim}; }<#else>null</#if>,
	optionsValue: <#if params.optionsValue??>function(option) { return ${params.optionsValue?trim}; }<#else>null</#if>,
	optionsAfterRender: <#if params.optionsAfterRender??>function(option) { return ${params.optionsAfterRender?trim}; }<#else>null</#if>
}} --><!-- /ko -->
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

<#assign optionsText>
	<#if params.optionsText??>
		${params.optionsText}
	<#elseif config.scoped["InvariantControlsConfiguration"]?? && config.scoped["InvariantControlsConfiguration"].select??>
		<#if config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsText"]??>
			${config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsText"]}
		</#if>
	<#else>
		return getValueTitle(option);
	</#if>
</#assign>

<#assign optionsValue>
	<#if params.optionsValue??>
		${params.optionsValue}
	<#elseif config.scoped["InvariantControlsConfiguration"]?? && config.scoped["InvariantControlsConfiguration"].select??>
		<#if config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsValue"]??>
			${config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsValue"]}
		</#if>
	</#if>
</#assign>

<#assign optionsAfterRender>
	<#if params.optionsAfterRender??>
		${params.optionsAfterRender}
	<#elseif config.scoped["InvariantControlsConfiguration"]?? && config.scoped["InvariantControlsConfiguration"].select??>
		<#if config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsAfterRender"]??>
			${config.scoped["InvariantControlsConfiguration"]["select"].attributes["optionsAfterRender"]}
		</#if>
	</#if>
</#assign>

<select data-bind='attr: { multiple: multiple, id: "${fieldId}" },
    options: options,
   
    optionsCaption: "${msg(optionsCaption?trim)}",
	<#if isFunctionContent(optionsText?trim)>optionsText: function(option) { ${optionsText?trim} },</#if>
	<#if isFunctionContent(optionsValue?trim)>optionsValue: function(option) { ${optionsValue?trim} },</#if>
	<#if isFunctionContent(optionsAfterRender?trim)>optionsAfterRender: function(option) { ${optionsAfterRender?trim} },</#if>

    selectedOptions: multipleValues, disable: protected,
    valueAllowUnset: true
'></select>


<#function isFunctionContent text>
	<#return text?has_content && text?contains("return") >
</#function>
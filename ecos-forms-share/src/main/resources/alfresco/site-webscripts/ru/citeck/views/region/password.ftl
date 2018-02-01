<#assign params = viewScope.region.params!{} />
<#assign textValueInput>
    <#if params.validation?? && params.validation == 'false'>textValue
    <#else>textValidationValue</#if>
</#assign>

<#assign disabled>
    <#if params.disabled??>
    ko.computed(function() { return ${params.disabled}; });
    <#else>protected</#if>
</#assign>

<input id="${fieldId}" type="password" data-bind="textInput: ${textValueInput?trim}, disable: ${disabled?trim}" />
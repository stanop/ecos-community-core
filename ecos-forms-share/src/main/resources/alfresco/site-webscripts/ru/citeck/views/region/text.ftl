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

<#assign maxlength>
    <#if params.maxlength??>
        maxlength="${params.maxlength}"
    </#if>
</#assign>

<input id="${fieldId}" type="text" data-bind="textInput: ${textValueInput?trim}, disable: ${disabled?trim}" ${maxlength}/>
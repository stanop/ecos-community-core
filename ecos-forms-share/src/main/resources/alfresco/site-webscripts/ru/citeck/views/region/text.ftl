<#assign params = viewScope.region.params!{} />
<#assign textValueInput>
    <#if params.validation?? && params.validation == 'false'>textValue
    <#else>textValidationValue</#if>
</#assign>

<input id="${fieldId}" type="text" data-bind="textInput: ${textValueInput?trim}, disable: protected" />
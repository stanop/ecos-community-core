<#assign parameters = viewScope.region.params!{} />
<#assign textValueInput>
    <#if parameters.validation?? && parameters.validation == 'false'>textValue
    <#else>textValidationValue</#if>
</#assign>

<textarea id="${fieldId}" data-bind="textInput: ${textValueInput?trim}, disable: protected" style="
    <#if parameters.width?has_content>width: ${parameters.width};</#if>
    <#if parameters.height?has_content>height: ${parameters.height};</#if>
"></textarea>
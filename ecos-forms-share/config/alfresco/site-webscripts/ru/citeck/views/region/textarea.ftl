<#assign parameters = viewScope.region.params!{} />
<textarea id="${fieldId}" data-bind="textInput: textValue, disable: protected" style="
    <#if parameters.width?has_content>width: ${parameters.width};</#if>
    <#if parameters.height?has_content>height: ${parameters.height};</#if>
"></textarea>
<#assign controlParams = viewScope.region.params>
<#assign step = controlParams.step!"any" />
<input id="${fieldId}" type="number" data-bind="textInput: textValue, disable: protected" step="${step}" />
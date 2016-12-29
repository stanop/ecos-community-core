<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-fileUploadControl">

<div id="${controlId}" class="file-upload-control" data-bind="fileUploadControl: { 
	<#if params.type??>type: '${params.type}',</#if>
	<#if params.properties??>properties: ${params.properties},</#if>
	
	value: multipleValues
}">
    <input id="${controlId}-fileInput" type="file" class="hidden" data-bind="attr: { multiple: multiple }">
    <button id="${controlId}-openFileUploadDialogButton" 
            class="file-upload-open-dialog-button" 
            data-bind="disable: protected">${msg(params.buttonTitle!"form.select.label")}</button>
</div>
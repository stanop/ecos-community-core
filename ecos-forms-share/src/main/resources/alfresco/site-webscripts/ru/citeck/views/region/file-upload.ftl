<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-fileUploadControl">

<#assign accept>
    <#if params.accept??>
        accept="${params.accept}"
    </#if>
</#assign>

<div id="${controlId}" class="file-upload-control" data-bind="fileUploadControl: {
	<#if params.type??>type: '${params.type}',</#if>
	<#if params.properties??>properties: ${params.properties},</#if>
	multiple: multiple,
	value: multipleValues,
	maxCount: ${params.maxCount!'0'},
	maxSize: ${params.maxSize!'0'},
	alowedFileTypes: '${params.alowedFileTypes!""}'
}">
    <input id="${controlId}-fileInput" type="file" class="hidden" data-bind="attr: { multiple: multiple }" ${accept}>
    <button id="${controlId}-openFileUploadDialogButton"
            class="file-upload-open-dialog-button"
            data-bind="disable: protected">${msg(params.buttonTitle!"form.select.label")}</button>
</div>

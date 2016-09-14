<#assign controlId = fieldId + "-fileUploadControl">

<div id="${controlId}" class="file-upload-control" data-bind="fileUploadControl: { value: lastValue, multiple: multiple }">
    <input id="${controlId}-fileInput" type="file" class="hidden" data-bind="attr: { multiple: multiple }">
    <button id="${controlId}-openFileUploadDialogButton" 
            class="file-upload-open-dialog-button" 
            data-bind="disable: protected">${msg("form.select.label")}</button>
</div>
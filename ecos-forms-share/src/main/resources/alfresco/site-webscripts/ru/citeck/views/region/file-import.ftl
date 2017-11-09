<#assign params = viewScope.region.params!{} />
<#assign source = params.source!"create-variants" />
<#assign createButtonTitle   = params.createButtonTitle!"button.create" />
<#assign importButtonTitle   = params.importButtonTitle!"button.import" />
<#assign importControlId = fieldId + "-fileUploadControl">
<#assign createButtonControlId = fieldId + "-createButtonControl">

<div id="${fieldId}">
    <div id="${importControlId}" class="file-upload-control" data-bind="fileUploadControl: {
        <#if params.type??>type: '${params.type}',</#if>
        <#if params.properties??>properties: ${params.properties},</#if>
        <#if params.importUrl??>importUrl: '${params.importUrl}',</#if>
        multiple: multiple,
        value: multipleValues
    }">
        <input id="${importControlId}-fileInput" type="file" class="hidden" data-bind="attr: { multiple: multiple }">
        <button id="${importControlId}-openFileUploadDialogButton"
                class="file-upload-open-dialog-button"
                data-bind="disable: protected">${msg(importButtonTitle)}</button>
    </div>
    <#if params.createVariantsVisibility??>
        <div id="${createButtonControlId}" class="create-object-control" data-bind="component: {
            name: 'createObjectButton',
            params: {
                scope: $data,
                source: '${source}',
                id: '${controlId}',
                buttonTitle: '${msg(createButtonTitle)}',
                parentRuntime: $root.key(),
                virtualParent: ${((params.virtualParent!"false") == "true")?string}
            }
        }" ></div>
    </#if>
</div>
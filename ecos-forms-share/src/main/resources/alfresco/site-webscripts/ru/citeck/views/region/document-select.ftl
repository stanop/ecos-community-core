<#assign controlId = fieldId + "-documentSelectControl">
<#assign params = viewScope.region.params!{} />

<div id="${controlId}" class="document-select-control"
     data-bind="component: { name: 'documentSelect', params: {
        id: $element.id,
        value: value,
        multiple: multiple,
        options: options,
        info: info,
        protected: protected,
        createVariantsVisibility: ${params.createVariantsVisibility!'true'},
        addable: ${params.addable!'[]'},
        siteId: '${params.siteId!""}',
        maxCount: ${params.maxCount!'0'},
        maxSize: ${params.maxSize!'0'},
        alowedFileTypes: '${params.alowedFileTypes!""}',
        type: '${params.type!"ecos:document"}'
    }}">
</div>
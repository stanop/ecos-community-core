<#assign controlId = fieldId + "-documentSelectControl">
<#assign params = viewScope.region.params!{} />

<#assign createVariantsVisibility = params.createVariantsVisibility!"true" />
<#assign addable = params.addable!"[]" />

<div id="${controlId}" class="document-select-control"
     data-bind="component: { name: 'documentSelect', params: {
        id: $element.id,
        value: value,
        multiple: multiple,
        options: options,
        createVariantsVisibility: ${createVariantsVisibility},
        addable: ${addable},
        <#if params.siteId??>siteId: '${params.siteId}',</#if>
    }}">
</div>
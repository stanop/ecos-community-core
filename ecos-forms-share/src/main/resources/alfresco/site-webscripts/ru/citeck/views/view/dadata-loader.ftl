<#assign isViewMode = (viewScope.view.mode == "view")/>
<#assign params = (viewScope.view.params)!{} />

<#if !isViewMode && !inlineEdit>
    <div class="dadata-loader" data-bind="component: { name: 'dadata-loader', params: {
            runtime: $root,
            attributes: ${params.attributes}
        }}">
    </div>
</#if>
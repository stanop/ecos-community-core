<#assign params = viewScope.region.params!{} />
<#assign buttons = params.buttons![] />

<div id="${fieldId}-buttons-control" class="buttons-control"
    data-bind="component: { name: 'task-buttons', params: {
        fieldId: $element.id,
        protected: protected,
        value: value,
        node: node,
        buttons: ${buttons}
    }}">
</div>
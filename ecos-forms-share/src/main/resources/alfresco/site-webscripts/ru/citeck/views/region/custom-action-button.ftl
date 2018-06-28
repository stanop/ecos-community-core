<#assign params = viewScope.region.params!{} />
<#assign buttonTitle = params.buttonTitle! />
<#assign outcome = params.outcome! />
<#assign taskType = params.taskType! />

<div id="${fieldId}-buttons-control" class="buttons-control"
    data-bind="component: { name: 'custom-action-button', params: {
        value: value,
        fieldId: $element.id,
        attribute: $data,
        buttonTitle: '${buttonTitle?trim}',
        outcome: '${outcome?trim}',
        taskType: '${taskType?trim}'
    }}">
</div>
<#assign controlId = fieldId + "-createObjectControl">
<#assign params = viewScope.region.params!{} />
<#assign source = params.source!"create-variants" />
<div id="${controlId}" class="create-object-control" data-bind="component: { 
	name: 'createObjectButton', 
	params: {
		attribute: name(),
		type: nodetype(),
		source: '${source}',
		value: lastValue, 
		id: '${controlId}',
		buttonTitle: '${msg("button.create")}',
		parentRuntime: $root.key(),
		virtualParent: ${((params.virtualParent!"false") == "true")?string}
	}
}" ></div>
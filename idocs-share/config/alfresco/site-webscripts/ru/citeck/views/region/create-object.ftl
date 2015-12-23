<#assign controlId = fieldId + "-createObjectControl">
<#assign params = viewScope.region.params!{} />
<#assign source = params.source!"create-variants" />

<#assign constraint = params.constraint!"" />
<#assign constraintMessage = params.constraintMessage!"" />

<div id="${controlId}" class="create-object-control" data-bind="component: { 
	name: 'createObjectButton', 
	params: {
		<#if constraint?has_content>constraint: ${constraint},</#if>
		<#if constraintMessage?has_content>constraintMessage: '${constraintMessage}',</#if>

		scope: $data,
		source: '${source}',
		id: '${controlId}',
		buttonTitle: '${msg("button.create")}',
		parentRuntime: $root.key(),
		virtualParent: ${((params.virtualParent!"false") == "true")?string}
	}
}" ></div>
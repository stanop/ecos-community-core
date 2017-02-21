<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-select2Control">

<#-- mode: simple, details -->
<#assign mode = params.mode!"simple">

<div id="${controlId}" class="select2-control" data-bind='component: { name: "select2", params: {
			element: $element,

			disabled: protected,
			multiple: multiple,
			options: options,
			value: value,

			mode: "${mode}",

		    optionsCaption: "${msg(optionsCaption?trim)}",
			optionsText: <#if params.optionsText??>function(option) { return ${params.optionsText?trim}; }<#else>null</#if>,
			optionsValue: <#if params.optionsValue??>function(option) { return ${params.optionsValue?trim}; }<#else>null</#if>,
			optionsAfterRender: <#if params.optionsAfterRender??>function(option) { return ${params.optionsAfterRender?trim}; }<#else>null</#if>,

			getValueTitle: function(value) { 
				return ko.computed(function() { 
					return $data.getValueTitle.call($data, value);
				}); 
			}
        }
    }'>
</div>
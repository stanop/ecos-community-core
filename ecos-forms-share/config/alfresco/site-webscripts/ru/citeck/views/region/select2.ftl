<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-select2Control">

<#assign optionParameters = [ "optionsText" ]>

<#-- mode: list, table -->
<#assign mode = params.mode!"list">

<#-- predicats: startsWith, contains -->
<#assign searchPredicat = params.searchPredicat!"startsWith">

<#-- step: 10, 15, 20 ...  -->
<#assign step = params.step!"10">

<div id="${controlId}" class="select2-control" data-bind='component: { name: "select2", params: {
			element: $element,

			disabled: protected,
			multiple: multiple,
			options: options,
			value: value,

			mode: "${mode}",
			searchPredicat: "${searchPredicat}",
			step: ${step},

			getValueTitle: function(value) { 
				return ko.computed(function() { 
					return $data.getValueTitle.call($data, value);
				}); 
			},

			<#list optionParameters as op>
				<#if params[op]??>
					${op}: function(option) {
						return ko.computed(function() {
							return ${params[op]};
						})
					},
				</#if>
			</#list>

			<#if params.options??>
				options: return ko.computed(function() {
					return ${params.options};
				})
			</#if>
        }
    }'>
</div>
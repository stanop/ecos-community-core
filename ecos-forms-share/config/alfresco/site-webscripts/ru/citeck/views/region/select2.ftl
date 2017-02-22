<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-select2Control">

<#-- mode: simple, details -->
<#assign mode = params.mode!"simple">

<#-- predicats: startsWith, contains -->
<#assign searchPredicat = params.searchPredicat!"contains">

<div id="${controlId}" class="select2-control" data-bind='component: { name: "select2", params: {
			element: $element,

			disabled: protected,
			multiple: multiple,
			options: options,
			value: value,

			mode: "${mode}",
			searchPredicat: "${searchPredicat}",

			getValueTitle: function(value) { 
				return ko.computed(function() { 
					return $data.getValueTitle.call($data, value);
				}); 
			}
        }
    }'>
</div>
<#assign params = viewScope.region.params!{} />
<#assign controlId = fieldId + "-journal2Control">

<#assign optionParameters = [ "optionsText", "optionsValue" ]>

<#-- createVariant & virtualParent -->
<#assign createVariantsVisibility = params.createVariantsVisibility!"false" />
<#assign createVariantsSource>
	<#if params.createVariantsSource?? || params.journalType??>
		${params.createVariantsSource!"journal-create-variants"}
	<#else>create-views</#if>
</#assign>
<#assign virtualParent = params.virtualParent!"false" />

<#-- predicats: startsWith, contains -->
<#assign searchPredicat = params.searchPredicat!"contains">

<#-- step: 10, 15, 20 ...  -->
<#assign step = params.step!"10">

<div id="${controlId}" class="journal2-control" data-bind='component: { name: "select2", params: {
			element: $element,

			disabled: protected,
			multiple: multiple,
			multipleValues: multipleValues,

			options: options,

			mode: "table",

			<#-- table mode parameters -->
	        <#if params.journalTypeId??>journalTypeId: "${params.journalTypeId}",</#if>
	        <#if params.defaultVisibleAttributes??>defaultVisibleAttributes: "${params.defaultVisibleAttributes}",</#if>
	        <#if params.defaultSearchableAttributes??>defaultSearchableAttributes: "${params.defaultSearchableAttributes}",</#if>

			<#-- list mode parameters -->
			searchPredicat: "${searchPredicat}",
			step: ${step},

	        <#-- Create Object parameters -->
	        createVariantsVisibility: ${createVariantsVisibility},
	        createVariantsSource: "${createVariantsSource?trim}",
	        virtualParent: ${virtualParent},

			<#if params.options??>
				forceOptions: function() {
					var attribute = $data, node = attribute.node();
					return ko.computed(function() { return ${params.options}; });
				},
			</#if>

			<#list optionParameters as op>
				<#if params[op]??>
					${op}: function(option) {
						return ko.computed(function() {
							return ${params[op]};
						})
					},
				</#if>
			</#list>

			getValueTitle: function(value) {
				return ko.computed(function() {
					return $data.getValueTitle.call($data, value);
				});
			}
        }
    }'>
</div>
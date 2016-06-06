<#assign params = viewScope.region.params!{} />
<#assign feature = params.feature!"description" />
<span data-bind="css: { hidden: $data['${feature}']() == null || $data['${feature}']() == '' }, attr: { title: $data['${feature}'] }">?</span>
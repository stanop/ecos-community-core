<#assign params = viewScope.region.params!{} />
<#assign feature = params.feature!"valid" />
<#assign message = params.message!"validationMessage" />
<span class="validation-message" data-bind="text: $data['${message}'](), css: { hidden: $data['${feature}']() }"></span>
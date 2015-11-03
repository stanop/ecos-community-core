<#assign params = viewScope.region.params!{} />
<#if params.text??>
<label for="${fieldId}"">${params.text?html}</label>
<#else/>
<#assign feature = params.feature!"title" />
<label for="${fieldId}" data-bind="text: $data['${feature}']() || name()"></label>
</#if>
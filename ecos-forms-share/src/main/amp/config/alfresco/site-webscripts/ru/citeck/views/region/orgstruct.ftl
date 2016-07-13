<#assign controlId = fieldId + "-orgstructControl">
<#assign params = viewScope.region.params!{} />

<#assign submitButtonTitle = msg("button.ok")>
<#assign cancelButtonTitle = msg("button.cancel")>

<div id="${controlId}" class="orgstruct-control" data-bind="orgstructControl: { 
                                                                value: value, 
                                                                multiple: multiple 
                                                            },
                                                            params: function() { 
                                                                return {
                                                                    <#if params.allowedAuthorityType??>
                                                                        allowedAuthorityType: '${params.allowedAuthorityType}',
                                                                    </#if>

                                                                    <#if params.allowedGroupType??>
                                                                        allowedGroupType: '${params.allowedGroupType}',
                                                                    </#if>

                                                                    submitButtonTitle: '${submitButtonTitle}',
                                                                    cancelButtonTitle: '${cancelButtonTitle}'
                                                                } 
                                                            }" >

    <button id="${controlId}-showVariantsButton" 
            class="orgstruct-control-show-variants" 
            data-bind="disable: protected">${msg("form.select.label")}</button>
</div>
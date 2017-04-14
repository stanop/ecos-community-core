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

                                                                    <#if params.excludeFields??>
                                                                        excludeFields: '${params.excludeFields}',
                                                                    </#if>

                                                                    <#if params.rootGroup??>
                                                                        rootGroup: '${params.rootGroup}',
                                                                    <#elseif params.rootGroupFunction??>
                                                                        rootGroupFunction: ${params.rootGroupFunction},
                                                                    </#if>

                                                                    submitButtonTitle: '${submitButtonTitle}',
                                                                    cancelButtonTitle: '${cancelButtonTitle}'
                                                                }
                                                            }" >

    <button id="${controlId}-showVariantsButton"
            class="orgstruct-control-show-variants"
            data-bind="disable: protected">${msg(params.buttonTitle!"form.select.label")}</button>
</div>
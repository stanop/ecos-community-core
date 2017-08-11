<#assign controlId = fieldId + "-orgstructControl">
<#assign params = viewScope.region.params!{} />

<#assign submitButtonTitle = msg("button.ok")>
<#assign cancelButtonTitle = msg("button.cancel")>

<#if config.scoped["InvariantControlsConfiguration"]?? &&
     config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
     config.scoped["InvariantControlsConfiguration"].orgstruct.childrenMap["user"]??>
         <#assign userLabel = config.scoped["InvariantControlsConfiguration"].orgstruct.childrenMap["user"][0].attributes["label"]>
</#if>

<#if config.scoped["InvariantControlsConfiguration"]?? &&
     config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
     config.scoped["InvariantControlsConfiguration"].orgstruct.childrenMap["group"]??>
        <#assign userLabel = config.scoped["InvariantControlsConfiguration"].orgstruct.childrenMap["group"][0].attributes["label"]>
</#if>

<#assign allExcludeFields>
    <#if config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeFields"]?? && params.excludeFields??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeFields"] + "," + params.excludeFields}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeFields"]??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeFields"]}
    <#elseif params.excludeFields??>
         ${params.excludeFields}
    <#else></#if>
</#assign>

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

                                                                    <#if allExcludeFields??>
                                                                        excludeFields: '${allExcludeFields?trim}',
                                                                    </#if>

                                                                    <#if params.rootGroup??>
                                                                        rootGroup: '${params.rootGroup}',
                                                                    <#elseif params.rootGroupFunction??>
                                                                        rootGroupFunction: ${params.rootGroupFunction},
                                                                    </#if>

                                                                    labels:
                                                                        {
                                                                            GROUP: '${groupLabel!""}',
                                                                            USER: '${userLabel!""}'
                                                                        },
                                                                    submitButtonTitle: '${submitButtonTitle}',
                                                                    cancelButtonTitle: '${cancelButtonTitle}'
                                                                }
                                                            }" >

    <button id="${controlId}-showVariantsButton"
            class="orgstruct-control-show-variants"
            data-bind="disable: protected">${msg(params.buttonTitle!"form.select.label")}</button>
</div>
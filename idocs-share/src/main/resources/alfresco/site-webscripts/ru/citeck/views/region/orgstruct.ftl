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
        <#assign groupLabel = config.scoped["InvariantControlsConfiguration"].orgstruct.childrenMap["group"][0].attributes["label"]>
</#if>

<#assign allExcludeAuthorities>
    <#if config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]?? && params.excludeAuthorities??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"] + "," + params.excludeAuthorities}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct?? &&
         config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]??>
             ${config.scoped["InvariantControlsConfiguration"].orgstruct.attributes["excludeAuthorities"]}
    <#elseif params.excludeAuthorities??>
         ${params.excludeAuthorities}
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

                                                                    <#if params.allowedGroupSubType??>
                                                                        allowedGroupSubType: '${params.allowedGroupSubType}',
                                                                    </#if>

                                                                    <#if allExcludeAuthorities??>
                                                                        excludeAuthorities: '${allExcludeAuthorities?trim}',
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
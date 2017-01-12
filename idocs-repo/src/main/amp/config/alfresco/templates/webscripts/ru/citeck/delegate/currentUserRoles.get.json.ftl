<#import "/ru/citeck/orgstruct/orgstruct.lib.ftl" as orgstruct>

<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list roles as role>
    <@orgstruct.renderAuthority role.group>,
    "manage": "${role.manage?string}",
    "delegate": "${role.delegate?string}"
    </@orgstruct.renderAuthority>
    <#if role_has_next>,</#if>
</#list>
]
</#escape>
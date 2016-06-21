<#import "/ru/citeck/orgstruct/orgstruct.lib.ftl" as orgstruct>

<#escape x as jsonUtils.encodeJSONString(x)>
[
    <#list branches as branch>
        <@orgstruct.renderAuthority branch/>
        <#if branch_has_next>,</#if>
    </#list>
]
</#escape>
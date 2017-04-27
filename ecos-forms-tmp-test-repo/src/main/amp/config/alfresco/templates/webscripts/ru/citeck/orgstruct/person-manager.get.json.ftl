<#import "orgstruct.lib.ftl" as orgstruct />
{
    "managerRole": <#if managerRole??><@orgstruct.renderAuthority managerRole /><#else>null</#if>,
    "managerUsers": <@orgstruct.renderAuthorities managerUsers />
}
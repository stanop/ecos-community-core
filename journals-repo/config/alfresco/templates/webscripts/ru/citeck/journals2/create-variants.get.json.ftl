<#import "journals.lib.ftl" as journals />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if siteId??>
        "siteId": "${siteId}",
    </#if>

    "createVariants": [
    <#list createVariants as createVariant>
        <@journals.renderCreateVariant createVariant /><#if createVariant_has_next>,</#if>
    </#list>
    ]
}
</#escape>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "createVariants": [
    <#list createViewClasses as viewClass>
    <#assign className = shortQName(viewClass) />
    {
        "type": "${className}",
        "title": "${nodeService.getClassTitle(viewClass)!className}",
        "formId": null,
        "destination": null,
        "canCreate": true,
        "isDefault": true
    }<#if viewClass_has_next>,</#if>
    </#list>
    ]
}
</#escape>
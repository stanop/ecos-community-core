<#escape x as jsonUtils.encodeJSONString(x)>
{
    "createVariants": [
    <#assign types = [] />
    <#list createViewClasses as viewClass>
    <#assign className = shortQName(viewClass) />
    {
        "type": "${className}",
        "title": "${nodeService.getClassTitle(viewClass)!className}",
        <#if !types?seq_contains(viewClass)>
            <#assign types = types + [viewClass] />
        </#if>
        "parentTypes": [
        <#list nodeService.getParentClasses(viewClass) as parentClass>
            "${shortQName(parentClass)}"<#if parentClass_has_next>,</#if>
            <#if !types?seq_contains(parentClass)>
                <#assign types = types + [parentClass] />
            </#if>
        </#list>
        ],
        "formId": null,
        "destination": null,
        "canCreate": true,
        "isDefault": true
    }<#if viewClass_has_next>,</#if>
    </#list>
    ],
    "typeNames": {
    <#list types as type>
        <#assign typeName = shortQName(type) />
        "${typeName}": "${nodeService.getClassTitle(type)!typeName}"<#if type_has_next>,</#if>
    </#list>
    }
}
</#escape>
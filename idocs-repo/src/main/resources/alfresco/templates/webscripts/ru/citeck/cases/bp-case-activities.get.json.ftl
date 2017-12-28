<#escape x as jsonUtils.encodeJSONString(x)>
{
"createVariants": [
    <#list variants as variant>
        {
            "type" : "${variant.type}",
            "title" : "${variant.title}",
            <#if variant.viewParams??>
                "viewParams" : {
                <#assign viewParams = variant.viewParams/>
                <#list viewParams?keys as viewParam>
                    "${viewParam}" : <#if viewParams[viewParam]??>"${viewParams[viewParam]}"<#else>null</#if>
                    <#if viewParam_has_next>,</#if>
                </#list>
                },
            </#if>
            "formId": null,
            "destination": null,
            "canCreate": true,
            "isDefault": true,
            "parentTypes" : [
                <#list variant.parentTypes as parentName>
                    "${parentName}"<#if parentName_has_next>,</#if>
                </#list>
            ]
        }<#if variant_has_next>,</#if>
    </#list>
],
"typeNames": {
    <#list viewTypeNames?keys as typeName>
        "${typeName}" : "${viewTypeNames[typeName]}"<#if typeName_has_next>,</#if>
    </#list>
}
}
</#escape>
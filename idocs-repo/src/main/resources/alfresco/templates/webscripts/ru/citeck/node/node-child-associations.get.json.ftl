<#import "/ru/citeck/search/search-macros.ftl" as search />
<#escape x as jsonUtils.encodeJSONString(x)>

{
    <#if nodeProperties??>
        <#-- Node properties -->
        <#list nodeProperties?keys as property>
            <#assign propertyName = shortQName(property)>
            "${propertyName}": <#if nodeProperties[property]??><@search.printValue nodeProperties[property]/><#else>null</#if>,
        </#list>
        <#-- Node child associations -->
        "childAssociations" : [
            <#list childAssociations as childAssociation>
                {
                <#-- Child association properties -->
                <#list childAssociation.properties as propertyEntry>
                    <#assign propertyName = shortQName(propertyEntry.key)>
                    "${propertyName}" : <#if propertyEntry.value??><@search.printValue propertyEntry.value/><#else>null</#if>
                    ,
                </#list>
                <#-- Child-child associations -->
                "childAssociations" : [
                    <#list childAssociation.childAssociations as childChildAssoc>
                        {
                            <#assign propertyName = shortQName(childChildAssoc.key)>
                            "name" : "${propertyName}",
                            <#-- Child-child properties -->
                            <#list childChildAssoc.value as propertyEntry>
                                <#assign propertyName = shortQName(propertyEntry.key)>
                                "${propertyName}" : <#if propertyEntry.value??><@search.printValue propertyEntry.value/><#else>null</#if>
                                <#if propertyEntry_has_next>,</#if>
                            </#list>
                        }<#if childChildAssoc_has_next>,</#if>
                    </#list>
                ]
                }<#if childAssociation_has_next>,</#if>
            </#list>
        ]
    </#if>
}
</#escape>
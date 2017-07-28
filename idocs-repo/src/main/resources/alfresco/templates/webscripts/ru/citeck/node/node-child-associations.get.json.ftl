<#import "/ru/citeck/search/search-macros.ftl" as search />
<#escape x as jsonUtils.encodeJSONString(x)>

{
    <#if nodeExists == true>
        <#-- Node child associations -->
        "nodes" : [
            <#list childAssociations as childAssociation>
                {
                "nodeRef" : "${childAssociation.nodeRef}",
                "parent": "${childAssociation.parentRef}",
                "contentUrl" : "${childAssociation.contentUrl}",
                <#-- Child association properties -->
                "attributes" : {
                <#list childAssociation.properties as propertyEntry>
                    <#assign propertyName = shortQName(propertyEntry.key)>
                    "${propertyName}" : <#if propertyEntry.value??><@search.printValue propertyEntry.value/><#else>null</#if><#if propertyEntry_has_next>,</#if>
                </#list>
                },
                <#-- Child-child associations -->
                "childAssociations" : [
                    <#list childAssociation.childAssociations as childChildAssoc>
                        {
                            <#assign propertyName = shortQName(childChildAssoc.key)>
                            "name" : "${propertyName}",
                            "nodeRef" : "${childChildAssoc.value.nodeRef}",
                            "parent": "${childChildAssoc.value.parentRef}",
                            "contentUrl" : "${childChildAssoc.value.contentUrl}",
                            "attributes" : {
                            <#-- Child-child properties -->
                            <#list childChildAssoc.value.properties as propertyEntry>
                                    <#assign propertyName = shortQName(propertyEntry.key)>
                                "${propertyName}" : <#if propertyEntry.value??><@search.printValue propertyEntry.value/><#else>null</#if>
                                <#if propertyEntry_has_next>,</#if>
                            </#list>
                            }
                        }<#if childChildAssoc_has_next>,</#if>
                    </#list>
                ]
                }<#if childAssociation_has_next>,</#if>
            </#list>
        ]
    </#if>
}
</#escape>
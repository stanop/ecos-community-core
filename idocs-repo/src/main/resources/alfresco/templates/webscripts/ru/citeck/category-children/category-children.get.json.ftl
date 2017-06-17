<#escape x as jsonUtils.encodeJSONString(x)>
{
    "totalResults": ${data?size?c},
    "data": [
        <#list data as item>
            {
                <#if item.hasChildren??>
                    "hasChildren": "${item.hasChildren?string}",
                </#if>
                <#if item.depth??>
                    "depth": "${item.depth?string}",
                </#if>
                <#if item.parentNodeRef??>
                    "parentNodeRef": "${item.parentNodeRef}",
                </#if>
                <#if item.hasPermission??>
                    "userAccess": {
                        "create": ${item.hasPermission("CreateChildren")?string},
                        "edit": ${item.hasPermission("Write")?string},
                        "delete": ${item.hasPermission("Delete")?string}
                    },
                </#if>
                <#if item.properties?? && item.properties.description??>
                    "description": "${item.properties.description}",
                </#if>
                <#if item.properties?? && item.properties.title??>
                    "title": "${item.properties.title}",
                </#if>
                    "nodeRef": "${item.nodeRef}",
                    "name": "${item.name}"                
            }<#if item_has_next>,</#if>
         </#list>
    ]
}
</#escape>
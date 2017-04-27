<#import "/ru/citeck/search/search-macros.ftl" as search />
<#escape x as jsonUtils.encodeJSONString(x)>
<#if data.addAssocs?? && data.addAssocs=="true">
"assocs":
[
</#if>
    <#list data.assocs as assoc>
    {
        "type": "${assoc.type}",
        "sources": [
            <#list assoc.sources as source> {
                "nodeRef": "${source.nodeRef}",
                "name": "${source.name}",
                "isFolder": "${source.isFolder}",
                "isContent": "${source.isContent}",
                "typeDirect": "sources",
                "title": "${source.title}",
                "attributes": {
                    <#if source.source.properties??>
                        <@search.propertiesJSON source.source.properties/>
                    </#if>
                    <#if source.source.associations??>
                        <#if (source.source.associations?size > 0 && source.source.properties?? && source.source.properties?size > 0)>
                            ,
                        </#if>
                            <@search.associationsJSON source.source.associations/>
                    </#if>
                }
            }<#if source_has_next>,</#if>
            </#list>
        ],
        "targets": [
            <#list assoc.targets as target> {
                "nodeRef": "${target.nodeRef}",
                "name": "${target.name}",
                "isFolder": "${target.isFolder}",
                "isContent": "${target.isContent}",
                "typeDirect": "targets",
                "title": "${target.title}",
                "attributes": {
                    <#if target.target.properties??>
                        <@search.propertiesJSON target.target.properties />
                    </#if>
                    <#if target.target.associations??>
                        <#if (target.target.associations?size > 0 && target.target.properties?? && target.target.properties?size > 0)>
                            ,
                        </#if>
                            <@search.associationsJSON target.target.associations/>
                    </#if>
                }
            }<#if target_has_next>,</#if>
            </#list>
        ],
        "children": [
            <#list assoc.children as child> {
                "nodeRef": "${child.nodeRef}",
                "name": "${child.name}",
                "isFolder": "${child.isFolder}",
                "isContent": "${child.isContent}",
                "typeDirect": "children",
                "title": "${child.title}",
                "attributes": {
                    <#if child.child.properties??>
                        <@search.propertiesJSON child.child.properties/>
                    </#if>
                    <#if child.child.associations??>
                        <#if (child.child.associations?size > 0 && child.child.properties?? && child.child.properties?size > 0)>
                            ,
                        </#if>
                            <@search.associationsJSON child.child.associations/>
                    </#if>
                }
            }<#if child_has_next>,</#if>
            </#list>
        ]
    }<#if assoc_has_next>,</#if>
    </#list>
<#if data.addAssocs?? && data.addAssocs=="true">
]
</#if>
</#escape>

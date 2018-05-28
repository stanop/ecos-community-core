<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodes": [
        <#list nodes as node>
        {
			"nodeRef": "${node.nodeRef}",
            "parent": <#if node.parent??>"${node.parent.nodeRef}"<#else>null</#if>,
            "type": "${node.getTypeShort()}",
            "classNames": [
                <#list nodeService.getNodeClasses(node) as className>
                "${shortQName(className)}"<#if className_has_next>,</#if>
                </#list>
            ],
            "isDocument": ${node.isDocument?string},
            "isContainer": ${node.isContainer?string}
        }<#if node_has_next>,</#if>
        </#list>
    ]
}
</#escape>


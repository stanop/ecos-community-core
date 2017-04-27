{"nodes": [
    <#list nodes as node>
    {
        "nodeRef": "${node.nodeRef}",
        "name": "${node.name}"
    }<#if node_has_next>,</#if>
    </#list>
]}
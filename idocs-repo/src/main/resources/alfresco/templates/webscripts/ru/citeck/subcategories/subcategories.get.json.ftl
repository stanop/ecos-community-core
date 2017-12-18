{"nodes": [
    <#list nodes as node>
    {
        "nodeRef": "${node.nodeRef}",
        "name": "${node.name}",
        "title": "${node.properties['cm:title']!node.name}"
    }<#if node_has_next>,</#if>
    </#list>
]}
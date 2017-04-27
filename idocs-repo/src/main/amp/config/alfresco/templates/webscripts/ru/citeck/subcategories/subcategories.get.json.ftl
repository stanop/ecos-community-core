{"nodes": [
    <#list nodes as node>
    {
        "nodeRef": "${node.nodeRef}",
        "name": "${node.name}"
        <#if node.getProperties()['title']??>,
            "title": "${node.getProperties()['title']}"
        <#else>
            "title": "${node.name}"
        </#if>
    }<#if node_has_next>,</#if>
    </#list>
]}
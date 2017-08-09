<#include "/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodes": [
        <#list nodes as node>
        {
			"nodeRef": "${node.nodeRef}",
            "parent": <#if node.parent??>"${node.parent.nodeRef}"<#else>null</#if>,
            "type": "${node.getTypeShort()}",
            "isDocument": ${node.isDocument?string},
            "isContainer": ${node.isContainer?string},
            "permissions": {
                <#assign permissions = nodeService.getAllowedPermissions(node) />
                <#list [ "CancelCheckOut", "ChangePermissions", "CreateChildren", "Delete", "Write" ] as permission>
                    <#if !permissions?seq_contains(permission)>
                    "${permission}": ${node.hasPermission(permission)?string},
                    </#if>
                </#list>
                <#list permissions as permission>
                    "${permission}": true<#if permission_has_next>,</#if>
                </#list>
            }
        }<#if node_has_next>,</#if>
        </#list>
    ]
}
</#escape>


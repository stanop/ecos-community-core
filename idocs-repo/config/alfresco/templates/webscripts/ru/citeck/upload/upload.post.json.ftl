<#include "/ecos-community/base-core-repo/config/alfresco/templates/webscripts/ru/citeck/search/search-macros.ftl">
<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if args["details"]?has_content && args["details"]?lower_case == "true">
        "nodeRef": "${document.nodeRef}",
        "parent": <#if document.parent??>"${document.parent.nodeRef}"<#else>null</#if>,
        "type": "${document.getTypeShort()}",
        "isDocument": ${document.isDocument?string},
        "isContainer": ${document.isContainer?string},
        "attributes": {
            <#assign attributes = nodeService.getAttributes(document) />
            <#list attributes?keys as key>
                "${key}": <#if !(attributes[key]??)>
                    null
                <#elseif nodeService.isContentProperty(attributes[key])>
                    <@printValue document.properties[key] />
                <#else>
                    <@printValue attributes[key] />
                </#if><#if key_has_next>,</#if>
            </#list>
        },
        "permissions": {
            <#assign permissions = nodeService.getAllowedPermissions(document) />
            <#list [ "CancelCheckOut", "ChangePermissions", "CreateChildren", "Delete", "Write" ] as permission>
                <#if !permissions?seq_contains(permission)>
                "${permission}": ${document.hasPermission(permission)?string},
                </#if>
            </#list>
            <#list permissions as permission>
                "${permission}": true<#if permission_has_next>,</#if>
            </#list>
        },
    <#else>
        "nodeRef": "${document.nodeRef}",
        "fileName": "${document.name}",    
    </#if>
   
   "status": {
      "code": 200,
      "name": "OK",
      "description": "File uploaded successfully"
   }
}
</#escape>


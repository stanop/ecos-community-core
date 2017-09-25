<#assign general = documents[0]>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "type": <#if general.node.properties['tk:type']??>"${general.node.properties['tk:type'].nodeRef}"<#else>null</#if>,
    "kind": <#if general.node.properties['tk:kind']??>"${general.node.properties['tk:kind'].nodeRef}"<#else>null</#if>,
    "uploaded": "${xmldate(general.uploaded)}",
    "uploader": {
        "userName": "${general.uploader.properties['cm:userName']}",
        "firstName": "${general.uploader.properties['cm:firstName']!}",
        "lastName": "${general.uploader.properties['cm:lastName']!}"
    },
    "documents": [
        <#list documents as document>
            {
                "nodeRef": "${document.node.nodeRef}",
                "name": "${document.node.name}"
            }<#if document_has_next>,</#if>
        </#list>
    ]
}
</#escape>

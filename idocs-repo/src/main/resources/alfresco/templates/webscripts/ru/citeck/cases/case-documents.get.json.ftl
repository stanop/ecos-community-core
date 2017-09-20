<#macro renderDocument document>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodeRef": "${document.nodeRef}",
    "name": "${document.properties['cm:title']!document.name}",
    "type": <#if document.properties['tk:type']??>"${document.properties['tk:type'].nodeRef}"<#else>null</#if>,
    "kind": <#if document.properties['tk:kind']??>"${document.properties['tk:kind'].nodeRef}"<#else>null</#if>,
    "uploaded": <#if document.properties.modified??>"${xmldate(document.properties.modified)}"<#else>null</#if>,
    "uploader": <#if document.properties.modifier??>{
        <#if people[document.properties.modifier]??>
        <#assign uploader = people[document.properties.modifier]/>
        "userName": "${uploader.properties['cm:userName']}",
        "firstName": "${uploader.properties['cm:firstName']!}",
        "lastName": "${uploader.properties['cm:lastName']!}"
        </#if>
    }<#else>null</#if>
}
</#escape>
</#macro>

<#macro renderContainer container>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodeRef": "${container.node.nodeRef}",
    "name": "<#if container.node.hasAspect('icase:subcase')>${container.node.assocs['icase:subcaseElement'][0].name}<#else>${container.node.name}</#if>",
    "kind": "${container.kind.nodeRef}",
    "containers": [
    <#list container.containers as item>
        <@renderContainer item /><#if item_has_next>,</#if>
    </#list>
    ],
    "documents": [
    <#list container.documents as item>
        <@renderDocument item /><#if item_has_next>,</#if>
    </#list>
    ]
}
</#escape>
</#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
{
    "permissions": {
        "createType": ${permissions.createType?string}
    },
    "container": <@renderContainer container />,
    "containerKinds": [
        <#list containerKinds?values as type>
        {
            "nodeRef": "${type.node.nodeRef}",
            "name": "${type.node.properties['cm:title']!type.node.name}",
            "documentKinds": [
                <#list type.documentKinds as kind>
                {
                    "nodeRef": <#if kind.kind??>"${kind.kind.nodeRef}"<#else>null</#if>,
                    "mandatory": ${kind.mandatory?string},
                    "multiple": ${kind.multiple?string}
                }<#if kind_has_next>,</#if>
                </#list>
            ]
        }<#if type_has_next>,</#if>
        </#list>
    ],
    "documentTypes": [
        <#list documentTypes?values as type>
        {
            <#if type??>
                "permissions": {
                    "createKind": "${type.hasPermission("CreateChildren")?string}"
                },
                "nodeRef": "${type.nodeRef}",
                "name": "${type.properties['cm:title']!type.name}"
            </#if>
        }<#if type_has_next>,</#if>
        </#list>
    ],
    "documentKinds": [
        <#list documentKinds?values as record>
        {
            "nodeRef": <#if record.kind??>"${record.kind.nodeRef}"<#else>null</#if>,
            "name": <#if record.kind??>"${record.kind.properties['cm:title']!record.kind.name}"<#else>null</#if>,
            "type": <#if record.type??>"${record.type.nodeRef}"<#else>null</#if>
        }<#if record_has_next>,</#if>
        </#list>
    ],
    "stages": [
        <#list stages as stage>
        "${stage}"<#if stage_has_next>,</#if>
        </#list>
    ]
}
</#escape>
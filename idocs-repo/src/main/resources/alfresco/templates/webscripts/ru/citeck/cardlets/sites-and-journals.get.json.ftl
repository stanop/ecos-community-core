<#macro stringOrNull node property>
    <#escape x as jsonUtils.encodeJSONString(x)>
        <#if node.properties[property]??>"${node.properties[property]}"<#else/>null</#if>
    </#escape>
</#macro>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "username": "${username}",
    "sites": [
    <#list sites as site>
        {
            "siteId": "${site.site.properties["cm:name"]}",
            "siteName": "${site.site.properties["cm:title"]}",
            "journals": [
            <#list site.journals as journal>
                {
                    "journalId": "${journal.properties["journal:journalType"]}",
                    "journalName": "${journal.properties["cm:title"]}"
                }<#if journal_has_next>,</#if>
            </#list>
            ]
        }<#if site_has_next>,</#if>
    </#list>
    ]
}
</#escape>
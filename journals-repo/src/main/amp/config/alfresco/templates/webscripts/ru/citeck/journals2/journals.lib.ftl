<#macro renderJournalType journalType>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "journalType": "${journalType.id}",
    "settings": <@renderJournalOptions journalType.options />,
    "attributes": [
        <#list journalType.attributes as attribute>
        {
        "name": "${attribute}",
        "isDefault":  ${journalType.isAttributeDefault(attribute)?string},
        "visible":    ${journalType.isAttributeVisible(attribute)?string},
        "searchable": ${journalType.isAttributeSearchable(attribute)?string},
        "sortable":   ${journalType.isAttributeSortable(attribute)?string},
        "groupable":  ${journalType.isAttributeGroupable(attribute)?string},
        "settings": <@renderJournalOptions journalType.getAttributeOptions(attribute) />
        }<#if attribute_has_next>,</#if>
        </#list>
    ]
}
</#escape>
</#macro>

<#macro renderJournalOptions options>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#list options?keys as name>
    "${name}": <#if options[name]??>"${options[name]}"<#else>null</#if><#if name_has_next>,</#if>
    </#list>
}
</#escape>
</#macro>

<#macro renderJournal journal full=true>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodeRef": "${journal.nodeRef}"
    , "title": "${journal.properties["cm:title"]!}"
    , "type": "${journal.properties["journal:journalType"]}"
    <#if full>
    , "criteria": <@renderCriteria journal.childAssocs["journal:searchCriteria"]![] />
    , "createVariants": 
    [
        <#if journal.childAssocs["journal:createVariants"]??>            
            <#list journal.childAssocs["journal:createVariants"] as createVariant>
                <@renderCreateVariant createVariant /><#if createVariant_has_next>,</#if>
            </#list>            
        </#if>
    ]
    </#if>
}
</#escape>
</#macro>

<#macro renderJournals journals full=false>
<#escape x as jsonUtils.encodeJSONString(x)>
[
    <#list journals as journal>
        <@renderJournal journal full /><#if journal_has_next>,</#if>
    </#list>
]
</#escape>
</#macro>

<#macro renderFilter filter full=true>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    "nodeRef": "${filter.nodeRef}"
    , "title": "${filter.properties["cm:title"]!}"
    , "permissions": <@renderPermissions filter />
    <#if full>
    , "journalTypes": <@renderJournalTypes filter.properties["journal:journalTypes"]![] />
    , "criteria": <@renderCriteria filter.childAssocs["journal:searchCriteria"]![] />
    </#if>
}
</#escape>
</#macro>

<#macro renderFilters filters full=false>
<#escape x as jsonUtils.encodeJSONString(x)>
[
    <#list filters as filter>
        <@renderFilter filter full /><#if filter_has_next>,</#if>
    </#list>
]
</#escape>
</#macro>

<#macro renderSettingsItem settings full=true>
<#escape y as jsonUtils.encodeJSONString(y)>
{
    "nodeRef": "${settings.nodeRef}"
    , "title": "${settings.properties["cm:title"]}"
    , "permissions": <@renderPermissions settings />
    <#if full>
    , "journalTypes": <@renderJournalTypes settings.properties["journal:journalTypes"]![] />
    , "visibleAttributes": [
    <#if settings.properties["journal:visibleAttributes"]??>
        <#list settings.properties["journal:visibleAttributes"] as attr>
        "${attr}"<#if attr_has_next>, </#if>
        </#list>
    </#if>
    ]
    , "maxRows": ${(settings.properties["journal:maxRows"]!10)?c}
    <#assign groupBy = settings.properties["journal:groupByAttribute"]! />
    <#assign sortBy = settings.properties["journal:sortByAttribute"]! />
    , "groupByAttribute": <#if groupBy != "">"${groupBy}"<#else>null</#if>
    , "sortByAttribute": <#if sortBy != "">"${sortBy}"<#else>null</#if>
    , "sortByAsc": ${(settings.properties["journal:sortByAsc"]!"null")?string}
    </#if>
}
</#escape>
</#macro>

<#macro renderSettingsList settingsList full=false>
<#escape x as jsonUtils.encodeJSONString(x)>
[
    <#list settingsList as settings>
        <@renderSettingsItem settings full /><#if settings_has_next>,</#if>
    </#list>
]
</#escape>
</#macro>

<#macro renderJournalTypes journalTypes>
<#escape y as jsonUtils.encodeJSONString(y)>
[ <#list journalTypes as journalType>"${journalType}"<#if journalType_has_next>,</#if> </#list>]
</#escape>
</#macro>

<#macro renderCriterion criterion>
<#assign valueTemplate = (criterion.properties["journal:criterionValue"]!"")?replace("#{","${")?interpret />
<#escape y as jsonUtils.encodeJSONString(y)>
{
    "field": "${shortQName(criterion.properties["journal:fieldQName"])}",
    "predicate": "${criterion.properties["journal:predicate"]}",
    "value": "<@valueTemplate/>"
}
</#escape>
</#macro>

<#macro renderCriteria criteria>
<#escape y as jsonUtils.encodeJSONString(y)>
[
<#list criteria as criterion>
    <@renderCriterion criterion /><#if criterion_has_next>,</#if>
</#list>
]
</#escape>
</#macro>

<#macro renderCreateVariant createVariant>
    <#escape x as jsonUtils.encodeJSONString(x)>
    {
        "title": "${createVariant.properties["cm:title"]!createVariant.properties["cm:name"]}",
        "destination": "${createVariant.assocs["journal:destination"][0].nodeRef}",
        "type": "${shortQName(createVariant.properties["journal:type"])}",
        "formId": "${createVariant.properties["journal:formId"]}",
        "canCreate": ${createVariant.assocs["journal:destination"][0].hasPermission("CreateChildren")?string("true","false")},
        "isDefault": ${(createVariant.properties["journal:isDefault"]!false)?string}
    }
    </#escape>
</#macro>

<#macro renderCreateVariants createVariants>
<#escape y as jsonUtils.encodeJSONString(y)>
[
<#list createVariants as createVariant>
    <@renderCreateVariant createVariant /><#if createVariant_has_next>,</#if>
</#list>
]
</#escape>
</#macro>

<#macro renderPermissions node>
<#escape y as jsonUtils.encodeJSONString(y)>
{
        <#list [ "Write", "Delete" ] as perm>
        "${perm}": ${node.hasPermission(perm)?string}<#if perm_has_next>,</#if>
        </#list>
}
</#escape>
</#macro>


{
    "datatype": "${args.datatype}",
    "predicates": [
        <#assign predicateList = findPredicateList(args.datatype) />
        <#if predicateList??>
            <#list predicateList.childrenMap["predicate"] as predicate>
                {
                    "id": "${predicate.value}",
                    "label": "${msg(predicate.attributes["label"]!"predicate." + predicate.value)}",
                    "needsValue": ${predicate.attributes["needsValue"]!("true")}
                }<#if predicate_has_next>,</#if>
            </#list>
        </#if>
    ]
}

<#function findPredicateList id>
    <#assign predicateLists = config.scoped["Search"]["predicate-lists"] />

    <#if predicateLists??>
        <#list predicateLists.childrenMap["predicate-list"] as predicateList>
            <#list predicateList.childrenMap["type"] as type><#if type.value == id><#return predicateList></#if></#list>
        </#list>
    </#if>
</#function>
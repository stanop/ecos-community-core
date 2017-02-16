{
    "datatype": "${args.datatype}",
    "predicates": [
    <#assign searchConfig = config.scoped["Search"]>
    <#assign predicateList = config.scoped["Search"]["predicate-lists"]/>
    <#if predicateList??>
        <#list predicateList.children as predicates>
            <#assign predicateListId = predicates.attributes["id"]/>
            <#if predicateListId != 'simpleBoolean'>
                <#list predicates.childrenMap["type"] as type>
                    <#if type.value == args.datatype >
                        <#list predicates.childrenMap["predicate"] as predicate>
                            {
                                "id": "${predicate.value}",
                                "label": "${msg(predicate.attributes["label"]!"predicate." + predicate.value)}",
                                "needsValue": ${predicate.attributes["needsValue"]!("true")}
                            }<#if predicate_has_next>,</#if>
                        </#list>
                    </#if>
                </#list>
            </#if>
        </#list>
    </#if>
    ]
}
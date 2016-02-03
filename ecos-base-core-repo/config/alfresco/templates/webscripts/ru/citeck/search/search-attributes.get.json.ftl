{
    "attributes": [
        <#list attributes as attribute>
        {
            "shortName": "${shortQName(attribute.fullName)}",
            "fullName": "${attribute.fullName}",
            "datatype": "${attribute.datatype}"
        }<#if attribute_has_next>,</#if>
        </#list>
    ]
}
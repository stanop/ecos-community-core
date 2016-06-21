[
<#escape x as jsonUtils.encodeJSONString(x)>
    <#list associations as assoc>
    {
        <#list assoc.props as prop>
            "name": "<#if (prop.fullQName)??>${prop.fullQName}</#if>",
            "prefixedName": "<#if (prop.shortQName)??>${prop.shortQName}</#if>",
            "title": "<#if (prop.title)??>${prop.title}</#if>"
        </#list> 
    }<#if assoc_has_next>,</#if>
    </#list>
</#escape>
]
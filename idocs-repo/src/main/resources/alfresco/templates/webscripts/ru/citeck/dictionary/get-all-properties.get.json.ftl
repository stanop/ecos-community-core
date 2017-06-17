[
<#escape x as jsonUtils.encodeJSONString(x)>
    <#list properties as property>
    {
        <#list property.props as prop>
            "name": "<#if (prop.fullQName)??>${prop.fullQName}</#if>",
            "prefixedName": "<#if (prop.shortQName)??>${prop.shortQName}</#if>",
            "title": "<#if (prop.title)??>${prop.title}</#if>"
        </#list> 
    }<#if property_has_next>,</#if>
    </#list>
</#escape>
]
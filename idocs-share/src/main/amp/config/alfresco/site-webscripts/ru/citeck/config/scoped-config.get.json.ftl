<#assign scopedConfig = config.scoped[scope]/>
<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#list scopedConfig?keys as configElementKey>
        <#if configElementKey != "forms" && configElementKey != "header">
            <#assign configElement = scopedConfig[configElementKey]/>
        <#if (configElementKey_index > 0)>,</#if>
<@printConfig configElement/>
        </#if>
    </#list>
}
</#escape>

<#macro printConfig configElement>
<#escape x as jsonUtils.encodeJSONString(x)>
    "${configElement.name}": {
    <#assign attributes = configElement.attributes/>
    <#if attributes?? && (attributes?size > 0) >
        "attributes": {
        <#list attributes?keys as attributeName>
            "${attributeName}": <@formatValue attributes[attributeName]/><#if attributeName_has_next>,</#if>
        </#list>
        },
    </#if>
    <#if configElement.value??>
        "value": <@formatValue configElement.value/>
    </#if>
    <#assign children = configElement.children/>
    <#if children?? && (children?size > 0)>
        "children": [
        <#list children as child>
            {
                <@printConfig child/>
            }<#if child_has_next>,</#if>
        </#list>
        ]
    </#if>
    }
</#escape>
</#macro>


<#macro formatValue value>
    <#escape x as jsonUtils.encodeJSONString(x)>
    <#if formatWtihMsg??>
        "${msg(value)}"
    <#else>
        "${value}"
    </#if>
    </#escape>
</#macro>
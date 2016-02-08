<#escape x as jsonUtils.encodeJSONString(x)>
{
    "attributes": {
    <#list metadata as md>
        "${md.propName}": {
                "name": "${md.propName}",
                "type": "<#if (md.type)??>${md.type}</#if>",
                "displayName": "<#if (md.displayName)??>${md.displayName}</#if>",
                "datatype": "<#if (md.datatype)??>${md.datatype}</#if>"<#if (md.labels)?size!=0>,
                "labels": {
                    <#list md.labels as labels>
                        <#assign label=labels?split(';')>
                        "${label[0]?html}": "${label[1]?html}"<#if labels_has_next>,</#if>
                    </#list>
                }</#if>
        }<#if md_has_next>,</#if>
    </#list>
    }
}
</#escape>

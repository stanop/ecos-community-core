<#escape x as jsonUtils.encodeJSONString(x)>
{
    "attributes": {
    <#list metadata as md>
        "${md.propName}": {
                "name": "${md.propName}",
                "type": "<#if (md.type)??>${md.type}</#if>",
                
                <#if md.nodetype??>
                    "nodetype": "${md.nodetype}",
                </#if>
                
                "displayName": "<#if (md.displayName)??>${md.displayName}</#if>",
                "datatype": "<#if (md.datatype)??>${md.datatype}</#if>"
                
                <#if (md.labels)?size!=0>,
                    "labels": {
                        <#list md.labels?keys as label>
                            "${label?html}": "${md.labels[label]?html}"<#if label_has_next>,</#if>
                        </#list>
                    }
                </#if>
        }<#if md_has_next>,</#if>
    </#list>
    }
}
</#escape>

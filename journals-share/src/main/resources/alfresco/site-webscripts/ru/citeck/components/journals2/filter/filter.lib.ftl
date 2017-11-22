
<#macro renderRegion region>
    <#if (viewScope.field.regions[region])??>
        <#global viewScope = viewScope + { "region" : viewScope.field.regions[region] } />
        <@renderElementTemplate "region" viewScope.field.regions[region].template />
    </#if>
</#macro>

<#macro renderCriterion htmlid criterion>

    <#assign template = criterion.template!"default" />

    <#global viewScope = { "field" : criterion } />
    <#global fieldId = htmlid />

    <@renderElementTemplate "criterion" criterion.template />
</#macro>

<#macro renderElementTemplate elementType template>
    <#assign file>/ru/citeck/components/journals2/filter/${elementType}/${template}.ftl</#assign>
    <#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
</#macro>
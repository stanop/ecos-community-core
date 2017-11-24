
<#macro renderRegion region>
    <#if (viewScope.field.regions[region])??>
        <#global viewScope = viewScope + { "region" : viewScope.field.regions[region] } />
        <@renderElementTemplate "region" viewScope.field.regions[region].template />
    </#if>
</#macro>

<#macro renderCriterion htmlid criterion>

    <#assign template = criterion.template!"default" />

    <#global viewScope = {
        "field": criterion,
        "view": {"mode": "edit"}
    } />
    <#global fieldId = htmlid />

    <@renderElementTemplate "criterion" criterion.template />
</#macro>

<#macro renderElementTemplate elementType template>
    <#assign file>/ru/citeck/components/journals2/filter/${elementType}/${template}.ftl</#assign>
    <#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
    <#if elementType == "region">
        <#assign file>/ru/citeck/views/region/edit/${template}.ftl</#assign>
        <#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
        <#assign file>/ru/citeck/views/region/${template}.ftl</#assign>
        <#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
    </#if>
    <!--TEMPLATE NOT FOUND elementType: "${elementType!}" template: "${template!}"-->
</#macro>
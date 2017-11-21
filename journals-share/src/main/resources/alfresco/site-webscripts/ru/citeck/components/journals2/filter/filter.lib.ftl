
<#macro renderRegion region>
    <@renderElementTemplate "region" region.template />
</#macro>

<#macro renderCriterion htmlid criterion>

    <#assign template = criterion.template!"default" />

    <#assign oldScope = viewScope!{} />
    <#global viewScope = oldScope + { "field" : criterion } />

    <#global fieldId = htmlid />

    <div class="criterion"
        <#--<#if element.attribute??>
         data-bind="css: {
            hidden: irrelevant,
            'with-help': description
         }"
        </#if>-->
    >
        <@renderElementTemplate "criterion" criterion.template />
    </div>

    <#global viewScope = oldScope />

</#macro>

<#macro renderElementTemplate elementType template>
    <#assign file>/ru/citeck/components/journal2/filter/${elementType}/${template}.ftl</#assign>
    <#if citeckUtils.templateExists(file)><#include file /><#return /></#if>
</#macro>
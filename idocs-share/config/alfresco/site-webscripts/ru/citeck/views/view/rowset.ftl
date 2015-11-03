<#if viewScope.view.params.title??>
    <div class="title" <#if viewScope.view.params.titleColor??>style="color:${viewScope.view.params.titleColor};"</#if>>
        <#if msg(viewScope.view.params.title)??>${msg(viewScope.view.params.title)}<#else>${viewScope.view.params.title}</#if>
    </div>
</#if>
<#include "default.ftl" />

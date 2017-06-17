<#import "deputies.lib.ftl" as lib />
[
<#list assistants as assistant>
    <@lib.renderAssistant assistant />
    <#if assistant_has_next>,</#if>
</#list>
]
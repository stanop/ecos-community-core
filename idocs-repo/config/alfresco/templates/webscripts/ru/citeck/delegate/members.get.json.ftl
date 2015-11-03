<#import "delegates.lib.ftl" as lib />
[
<#list members as member>
<@lib.renderMember member />
<#if member_has_next>,</#if>
</#list>
]
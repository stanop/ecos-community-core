<#import "delegates.lib.ftl" as lib />
[
<#list roles as role>
<@lib.renderRole role />
<#if role_has_next>,</#if>
</#list>
]
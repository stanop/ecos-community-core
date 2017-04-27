<#import "delegates.lib.ftl" as lib />
[
<#list delegates as delegate>
	<@lib.renderDelegate delegate />
	<#if delegate_has_next>,</#if>
</#list>
]
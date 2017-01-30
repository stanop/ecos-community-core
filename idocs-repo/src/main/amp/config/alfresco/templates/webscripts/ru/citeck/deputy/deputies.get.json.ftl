<#import "deputies.lib.ftl" as lib />
[
<#list deputies as deputy>
	<@lib.renderDeputy deputy />
	<#if deputy_has_next>,</#if>
</#list>
]
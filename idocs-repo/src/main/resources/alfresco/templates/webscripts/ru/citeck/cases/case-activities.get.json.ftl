<#include "case-activities.lib.ftl" />
{
	"activities": [
		<#list activities as activity>
			<@renderActivity activity /><#if activity_has_next>,</#if>
		</#list>
	]
}

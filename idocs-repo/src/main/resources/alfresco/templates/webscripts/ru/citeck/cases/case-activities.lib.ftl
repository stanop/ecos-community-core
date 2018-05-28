<#macro renderActivity activity>

	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"nodeRef": "${activity.nodeRef}",
		"type": "${activity.typeShort}",
		"index": <#if activity.properties["activ:index"]??>${activity.properties["activ:index"]?c}<#else>null</#if>,
		"title": "${activity.properties.title!}",
		"typeTitle": "${nodeService.getClassTitle(activity.typeShort)!activity.typeShort}",
		"description": "${activity.properties.description!}",
		"plannedStartDate": <#if activity.properties["activ:plannedStartDate"]??>"${xmldate(activity.properties["activ:plannedStartDate"])}"<#else>null</#if>,
		"plannedEndDate": <#if activity.properties["activ:plannedEndDate"]??>"${xmldate(activity.properties["activ:plannedEndDate"])}"<#else>null</#if>,
		"actualStartDate": <#if activity.properties["activ:actualStartDate"]??>"${xmldate(activity.properties["activ:actualStartDate"])}"<#else>null</#if>,
		"actualEndDate": <#if activity.properties["activ:actualEndDate"]??>"${xmldate(activity.properties["activ:actualEndDate"])}"<#else>null</#if>,
    	"expectedPerformTime": <#if activity.properties["activ:expectedPerformTime"]??>${activity.properties["activ:expectedPerformTime"]?c}<#else>null</#if>,


    "startable": <#if (activity.properties["activ:manualStarted"]!false) && ((activity.properties["activ:repeatable"]!false) || !(activity.properties["activ:actualStartDate"]??))>true<#else>false</#if>,
		"stoppable": <#if (activity.properties["activ:manualStopped"]!false) && (activity.properties["activ:actualStartDate"]??) && !(activity.properties["activ:actualEndDate"]??)>true<#else>false</#if>,
		"editable": <#if activity.hasPermission("Write")>true<#else>false</#if>,
		"removable": <#if activity.hasPermission("Delete")>true<#else>false</#if>,
		"composite": ${activity.hasAspect("activ:hasActivities")?string}
	}
	</#escape>
</#macro>

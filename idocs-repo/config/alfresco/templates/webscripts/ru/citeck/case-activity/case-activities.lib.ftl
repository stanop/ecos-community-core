<#macro renderActivity activity>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"nodeRef": "${activity.nodeRef}",
		"type": "${activity.typeShort}",
		"title": "${activity.properties.title!}",
		"typeTitle": "${nodeService.getClassTitle(activity.typeShort)!activity.typeShort}",
		"description": "${activity.properties.description!}",
		"plannedStartDate": <#if activity.properties["activ:plannedStartDate"]??>"${xmldate(activity.properties["activ:plannedStartDate"])}"<#else>null</#if>,
		"plannedEndDate": <#if activity.properties["activ:plannedEndDate"]??>"${xmldate(activity.properties["activ:plannedEndDate"])}"<#else>null</#if>,
		"actualStartDate": <#if activity.properties["activ:actualStartDate"]??>"${xmldate(activity.properties["activ:actualStartDate"])}"<#else>null</#if>,
		"actualEndDate": <#if activity.properties["activ:actualEndDate"]??>"${xmldate(activity.properties["activ:actualEndDate"])}"<#else>null</#if>,
		
		"startable": <#if activity.properties["activ:manualStarted"] && !(activity.properties["activ:actualStartDate"]??)>true<#else>false</#if>,
		"stoppable": <#if activity.properties["activ:manualStopped"] && activity.properties["activ:actualStartDate"]?? && !(activity.properties["activ:actualEndDate"]??)>true<#else>false</#if>,
		"editable": <#if !(activity.properties["activ:actualEndDate"]??) && activity.hasPermission("Write")>true<#else>false</#if>,
		"removable": <#if !(activity.properties["activ:actualEndDate"]??) && activity.hasPermission("Delete")>true<#else>false</#if>,
		"composite": ${activity.hasAspect("activ:hasActivities")?string}
	}
	</#escape>
</#macro>
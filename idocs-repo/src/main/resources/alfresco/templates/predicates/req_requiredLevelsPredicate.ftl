<#if predicate.assocs['req:requiredLevels']??>
<#assign requiredLevelNodeRef = predicate.assocs['req:requiredLevels'][0].nodeRef.toString() />
<#assign levelRequired = predicate.properties['req:levelRequired']!true />
(function() {
	var levels = completeness.getCompletedLevels(document);
	for (var i in levels)
	{
		if(levels[i].nodeRef.toString() == "${requiredLevelNodeRef}")
		{
			return ${levelRequired?string};
		}
	}
	return ${(!levelRequired)?string};
})()
</#if>
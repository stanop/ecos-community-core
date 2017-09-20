<#if predicate.assocs['pred:completedLevels']??>
(function() {
	if(space.assocs['req:completedLevels'])
	{
		for (var r in space.assocs['req:completedLevels'])
		{
			if(space.assocs['req:completedLevels'][r].nodeRef.toString() == "${predicate.assocs['pred:completedLevels'][0].nodeRef}")
			{
				return true;
			}
		}
	}
	if(space.hasAspect('icase:subcase') && space.parent.assocs['req:completedLevels'])
	{
		for (var j in space.parent.assocs['req:completedLevels'])
		{
			if(space.parent.assocs['req:completedLevels'][j].nodeRef.toString() == "${predicate.assocs['pred:completedLevels'][0].nodeRef}")
			{
				return true;
			}
		}
	}
	return false;
})()
</#if>
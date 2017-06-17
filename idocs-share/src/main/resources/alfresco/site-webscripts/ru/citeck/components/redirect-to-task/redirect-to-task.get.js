<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

(function() 
{
    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);

	var taskNode = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
	var redirectURL = '';
	if(taskNode.item.node.properties["bpm:status"]=='Completed')
	{
		redirectURL='/share/page/task-details?taskId='+taskNode.item.node.properties["cm:name"];
	}
	else
	{
		if(taskNode.item.node.properties["wfm:document"])
		{
			redirectURL='/share/page/card-details?nodeRef='+taskNode.item.node.properties["wfm:document"];
		}
		else
		{
			redirectURL='/share/page/task-edit?taskId='+taskNode.item.node.properties["cm:name"];
		}
	}
	status.code = 303;
	status.location = redirectURL;
})();

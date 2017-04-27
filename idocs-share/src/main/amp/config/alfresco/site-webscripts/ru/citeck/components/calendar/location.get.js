<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
(function(){
	var CALENDAR_VIEW = 'day';

	if(!args.nodeRef)
		AlfrescoUtil.error(400, 'Calendar event node reference is not specified.');

	var connector = remote.connect("alfresco");
	var result = connector.get('/citeck/node?nodeRef=' + args.nodeRef + '&props=ia:fromDate');
	if (result.status != 200)
		AlfrescoUtil.error(result.status, 'Can not get calendar event info. nodeRef=' + args.nodeRef);

	var json = result != "" ? eval('(' + result + ')') : "";
	if (!json.siteShortName)
		AlfrescoUtil.error(400, 'Specified calendar event does not contain site name. nodeRef=' + args.nodeRef);

	if (!json.props['ia:fromDate'])
		AlfrescoUtil.error(400, 'Specified calendar event does not contain ia:fromDate. nodeRef=' + args.nodeRef);

	var dateRegexp = /^(\d\d\d\d-\d\d-\d\d).*?$/g,
		match = dateRegexp.exec(json.props['ia:fromDate']);

	if (match == null || !match[1])
		AlfrescoUtil.error(400, 'Can not parse date yyyy-mm-dd. date=' + json.props['ia:fromDate']);

	status.code = 303;
	status.location = '/share/page/site/' + json.siteShortName + '/calendar?view=' + CALENDAR_VIEW + '&date=' + match[1];
})();

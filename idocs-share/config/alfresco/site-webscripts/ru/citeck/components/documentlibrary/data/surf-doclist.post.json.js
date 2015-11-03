<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/data/surf-doclist.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/documentlibrary/data/surf-doclist-custom.lib.js">

var surfDoclist_main = function surfDoclist_main()
{
	var result = "{}";
	var dataUrl = DocList_Custom.calculateRemoteDataURL();
	var body = json ? json : new Packages.org.json.JSONObject();
	var connector = remote.connect("alfresco");
	var result = connector.post(dataUrl, body.toString(), "application/json");

	if (result.status == 200) {
		var obj = eval('(' + result + ')');
		if (obj && (obj.item || obj.items)) {
			DocList.processResult(obj, {
				actions: true,
				indicators: true,
				metadataTemplate: true
			});
			result = jsonUtils.toJSONString(obj);
		}
	}
	else {
		status.setCode(result.status);
	}
	model.json = result;
};

surfDoclist_main();

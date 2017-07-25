<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="/ru/citeck/components/common/config-params-to-ftl.inc.js">
<import resource="/ru/citeck/components/common/config-destination-resolver.inc.js">

/**
 * It gets input parameters from extension module with
 * {@code <url>/citeck/components/document-children</url>}
 * 
 * @param nodeRef - it is node reference of the item, which children will be shown.
 * 					it is related with {@code childrenUrl}, internal model can use
 * 					this parameter when getting root URL (substituting inner parameters).
 * @param childrenUrl - it is a root URL of represented children, it can contain
 * 					inner parameter {@code \{nodeRefForURL\}}
 * @returns
 */
function main() {
	var nodeRef = AlfrescoUtil.param('nodeRef', ''),
		childrenUrl = AlfrescoUtil.param('childrenUrl', ''),
		columns = AlfrescoUtil.param('columns', ''),
		responseSchema = AlfrescoUtil.param('responseSchema', ''),
		responseType = AlfrescoUtil.param('responseType', null),
		hideEmpty = AlfrescoUtil.param('hideEmpty', false),
		groupBy = AlfrescoUtil.param('groupBy', null),
        propertyName = AlfrescoUtil.param('propertyName', null),
        propertyValue = AlfrescoUtil.param('propertyValue', null),
		groupTitle = AlfrescoUtil.param('groupTitle', null),
		childrenFormat = AlfrescoUtil.param('childrenFormat', null),
    	availableButtonForGroups = AlfrescoUtil.param('childrenFormat', null);

	if (!childrenUrl) {
		AlfrescoUtil.error(400, "Input parameter 'childrenUrl' is empty");
		return;
	}
	if (!columns) {
		AlfrescoUtil.error(400, "Input parameter 'columns' is empty");
		return;
	}
	if (!responseSchema) {
		AlfrescoUtil.error(400, "Input parameter 'responseSchema' is empty");
		return;
	}

	model.params.destination = model.destination;
	if (!model.destinationPermissionsResult)
		model.params.buttonsInHeader = "";
}

main();


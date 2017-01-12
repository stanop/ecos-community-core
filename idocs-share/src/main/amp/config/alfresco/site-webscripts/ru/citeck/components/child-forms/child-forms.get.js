<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="/ru/citeck/components/common/config-params-to-ftl.inc.js">
<import resource="/ru/citeck/components/common/config-destination-resolver.inc.js">

model.params.destination = model.destination;
if (!model.destinationPermissionsResult)
	model.params.buttonsInHeader = "";

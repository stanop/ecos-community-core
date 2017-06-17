<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getDocumentWorkflows(nodeRef)
{
   if (args.active == "true") {
      var result = remote.call("/api/node/" + nodeRef.replace(":/", "") + "/workflow-instances");
   } else {
      var result = remote.call("/api/node/" + nodeRef.replace(":/", "") + "/inactive-workflow-instances");
   }
   if (result.status != 200)
   {
      AlfrescoUtil.error(result.status, 'Could not load inactive document workflows for ' + nodeRef);
   }
   var workflows = eval('(' + result + ')').data;
   if(args.sortBy) {
      workflows =  workflows.sort(function(a,b) {
        var x = a[args.sortBy],
            y = b[args.sortBy];
        return x == y ? 0 : x < y ? -1 : 1;
      });
   }
   return workflows;
}

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      model.destination = documentDetails && documentDetails.item && documentDetails.item.parent && documentDetails.item.parent.nodeRef || null;
      model.workflows = getDocumentWorkflows(model.nodeRef);
   }
}

main();

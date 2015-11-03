<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param("nodeRef");
   AlfrescoUtil.param("site", null);
   AlfrescoUtil.param("rootPage", "documentlibrary");
   AlfrescoUtil.param("rootLabelId", "path.documents");
   AlfrescoUtil.param("showFavourite", "false");
   AlfrescoUtil.param("showLikes", "false");
   AlfrescoUtil.param("showComments", "false");
   AlfrescoUtil.param("showDownload", "true");
   AlfrescoUtil.param("showPath", "false");
   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (nodeDetails)
   {
      model.item = nodeDetails.item;
      model.node = nodeDetails.item.node;
      model.isContainer = nodeDetails.item.node.isContainer;
      model.paths = AlfrescoUtil.getPaths(nodeDetails, model.rootPage, model.rootLabelId);
      model.showComments = (model.isContainer && nodeDetails.item.node.permissions.user["CreateChildren"] && model.showComments).toString();
      model.showDownload = (!model.isContainer && model.showDownload).toString();
      model.showComments = (model.isContainer).toString();
      model.showLikes = (model.isContainer).toString();
	  model.showPath = nodeDetails.item.parent ? "true" : "false";
      var count = nodeDetails.item.node.properties["fm:commentCount"];
      model.commentCount = (count != undefined ? count : null);
   }
}

main();
doclistDataUrlResolver = null;

DocList_Custom =
{
   calculateActionGroupId: function calculateActionGroupId(record, view)
   {
      // Default calculation
      var actionGroupId = (record.node.isContainer ? "folder-" : "document-") + (record.node.isLink ? "link-" : "") + view;

      if (logger.isLoggingEnabled())
         logger.log("[SURF-DOCLIST] ActionGroupId = '" + actionGroupId + "' for nodeRef " + record.node.nodeRef);

      return actionGroupId;
   }, 

      calculateRemoteDataURL: function calculateRemoteDataURL()
      {
         if (!doclistDataUrlResolver)
         {
            doclistDataUrlResolver = resolverHelper.getDoclistDataUrlResolver(config.scoped["DocumentLibrary"]["doclist"].childrenMap["data-url-resolver"].get(0).value)
         }
         return (doclistDataUrlResolver.resolve(url.templateArgs.webscript, url.templateArgs.params, args) + "");
      }
};
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/documentlibrary/data/surf-doclist-custom.lib.js">

function main()
{
    AlfrescoUtil.param('nodeRef');
    AlfrescoUtil.param('site', null);
    AlfrescoUtil.param('container', 'documentLibrary');
    AlfrescoUtil.param('actionLinkClass', 'action-link');

    var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site,
        {
            actions: true
        });
    if (documentDetails)
    {
        model.documentDetails = true;
        doclibCommon();
    }

    model.syncMode = syncMode.getValue();

    // Widget instantiation metadata...
    var documentActions = {
        id : "DocumentActions",
        name : "Citeck.NodeActions",
        options : {
            nodeRef : model.nodeRef,
            siteId : (model.site != null) ? model.site : null,
            containerId : model.container,
            rootNode : model.rootNode,
            replicationUrlMapping : (model.replicationUrlMappingJSON != null) ? model.replicationUrlMappingJSON : "{}",
            documentDetails : documentDetails,
            repositoryBrowsing : (model.rootNode != null),
            syncMode : model.syncMode != null ? model.syncMode : "",
            actionLinkClass : model.actionLinkClass,
			view : args.view || "details"
        }
    };
    model.widgets = [documentActions];
}

main();

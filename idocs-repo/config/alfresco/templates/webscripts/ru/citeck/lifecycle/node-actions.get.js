(function() {
    var nodeRef = args.nodeRef;
    var actions = nodeActionsService.getActions(nodeRef);
    model.actions = [];
    logger.log("Actions collection size: " + actions.size());
    for (var i = 0; i < actions.size(); i++) {
        model.actions.push({
            "node" : nodeRef,
            "url": actions.get(i)["url"],
            "title": actions.get(i)["title"]
        });
    }
})();
(function() {
    var nodeRef = args.nodeRef;
    var actions = nodeActionsService.getActions(nodeRef);
    model.actions = [];
    logger.log("Actions collection size: " + actions.size());
    for (var i = 0; i < actions.size(); i++) {
        var title = msg.get(actions.get(i)["title"]);

        // lifecycle - crutch
        if (title && title.indexOf("lifecycle.action.") == 0) {
            title = title.substring("lifecycle.action.".length)
        }

        model.actions.push({
            "node" : nodeRef,
            "url": actions.get(i)["url"],
            "title": title,
            "actionType": actions.get(i)["actionType"]
        });
    }
})();
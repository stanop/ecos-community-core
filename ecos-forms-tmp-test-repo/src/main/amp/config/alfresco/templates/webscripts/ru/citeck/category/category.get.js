(function() {
    if (args.nodeRef) {
        var category = classification.getCategory(args.nodeRef);
        model.data = {
            name: category.name
        }
    } else {
        status.setCode(status.STATUS_BAD_REQUEST, "Source node is not specified");
        return;
    }
})()
(function() {
    if (args.nodeRef) {
        var route = search.findNode(args.nodeRef);
        if (route) {
            route.remove();
            model.status = 200;
        } else {
            model.status = 404;
        }
    } else {
        model.status = 400;
    }

    model.message = status.message;
})()
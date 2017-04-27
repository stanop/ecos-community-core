(function() {
    var nodeRef = args.nodeRef;
    var type = args.type;
    var lcformat = args.lcformat;
    if(!nodeRef || !type || !lcformat) {
        model.success = false;
        return;
    }

    lifecycle.createTableFromFile(nodeRef, type, lcformat);
    model.success = true;
})();
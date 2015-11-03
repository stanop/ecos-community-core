(function() {
    var nodeRef = args.nodeRef;
    var type = args.type;
    if(!nodeRef || !type) {
        model.success = false;
        return;
    }
    lifecycle.createTableFromCSV(nodeRef, type);
    model.success = true;
})();
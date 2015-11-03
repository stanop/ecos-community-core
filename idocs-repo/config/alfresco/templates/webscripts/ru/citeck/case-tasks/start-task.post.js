(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        caseTaskService.startTask(nodeRef);
    }
    model.success = true;
})();
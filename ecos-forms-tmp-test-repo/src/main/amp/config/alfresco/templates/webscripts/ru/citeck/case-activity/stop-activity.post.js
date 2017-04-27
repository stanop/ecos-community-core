(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        caseActivityService.stopActivity(nodeRef);
    }
    model.success = true;
})();
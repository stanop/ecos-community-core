(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        caseActivityService.reset(nodeRef);
    }
    model.success = true;
})();
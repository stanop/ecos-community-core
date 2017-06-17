(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        caseActivityService.startActivity(nodeRef);
    }
    model.success = true;
})();
(function() {
    var nodeRef = args.nodeRef;
    if (!nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument nodeRef has not been provided.");
        return;
    }

    var journalAttributes = search.findNode(nodeRef);
    if (!journalAttributes) {
        status.setCode(status.STATUS_NOT_FOUND, "Journal settings [" + nodeRef + "] not found");
        return;
    }

    model.journalAttributes = journalAttributes;
})();
(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        var eventType = args.eventType;
        if (!eventType) {
            status.setStatus(status.STATUS_BAD_REQUEST, "Argument eventType is missing");
            return;
        }
        caseActivityEventService.fireEvent(nodeRef, eventType);
    } else {
        var eventRef = args.eventRef;
        if (!eventRef) {
            status.setStatus(status.STATUS_BAD_REQUEST, "At least one of 'nodeRef' or 'eventRef' must be specified!");
            return;
        }
        caseActivityEventService.fireConcreteEvent(eventRef);
    }
})();

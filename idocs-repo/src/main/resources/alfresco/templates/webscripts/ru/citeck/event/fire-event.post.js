(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef) {
        var eventType = args.eventType;
        if (!eventType) {
            status.setStatus(status.STATUS_BAD_REQUEST, "Argument eventType is missing");
            return;
        }
        if(args.document) {
            events.fireEvent(nodeRef, args.document, eventType);
        } else {
            events.fireEvent(nodeRef, eventType);
        }
    } else {
        var eventRef = args.eventRef;
        if (!eventRef) {
            status.setStatus(status.STATUS_BAD_REQUEST, "At least one of 'nodeRef' or 'eventRef' must be specified!");
            return;
        }
        if(args.document) {
            events.fireConcreteEvent(eventRef, args.document);
        } else {
            events.fireConcreteEvent(eventRef);
        }
    }
})();
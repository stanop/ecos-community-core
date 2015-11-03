(function() {
    var nodeRef = args.nodeRef;
    if (nodeRef && nodeRef!="" && nodeRef!=null) {
        var journal = search.findNode(nodeRef);
        if(journal!=null && journal!="" && journal)
        {
            model.journal = journal;
        }
        else
        {
            status.setCode(status.STATUS_NOT_FOUND, "There is no node found for specified nodeRef.");
            return;
        }
    }
    else
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument nodeRef has not been provided.");
        return;
    }
})();
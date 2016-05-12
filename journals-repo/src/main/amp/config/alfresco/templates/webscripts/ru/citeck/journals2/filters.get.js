(function() {
    var query = 'TYPE:"journal:filter"';
    var journalType = args.journalType;
    if (journalType && journalType!=null && journalType!="") {
        query += ' AND @journal\\:journalTypes: "' + journalType + '"';
    }
    else
    {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument journalType has not been provided.");
        return;
    }

    var filters = search.luceneSearch(query);
  	model.journalType = journalType;
    model.filters = filters;
})();
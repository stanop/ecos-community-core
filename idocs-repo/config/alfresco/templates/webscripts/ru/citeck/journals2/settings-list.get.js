(function() {
    var query = 'TYPE:"journal:settings"';
    var journalType = args.journalType;

    if (journalType && journalType!=null && journalType!="") {
        query += ' AND @journal\\:journalTypes: "' + journalType + '"';
    } else {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument journalType has not been provided.");
        return;
    }

    var journalsSettings = search.luceneSearch(query);
    model.journalType = journalType;
    model.journalsSettings = journalsSettings;
})();

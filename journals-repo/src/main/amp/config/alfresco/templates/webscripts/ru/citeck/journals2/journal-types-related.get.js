(function() {
    var journalTypes = journals.getAllJournalTypes();

    if(args.filter) {
        var filteredJournalTypes = [];
        for(var i in journalTypes) {
            if((journalTypes[i].id + "").indexOf(args.filter + "") != -1) {
                filteredJournalTypes.push(journalTypes[i]);
            }
        }
        journalTypes = filteredJournalTypes;
    }

    var mapTypes = [];
    for(var i in journalTypes) {
        var mapType = {};
        var journalTypeId = journalTypes[i].id;
        var journalType = journals.getJournalType(journalTypeId);
        mapType["journalTypes"] = journalTypeId;
        mapType["type"] = journalType.getOptions()["type"];
        mapTypes.push(mapType);
    }

    model.mapTypes = mapTypes;

})();
(function() {
    var journalTypes = journals.getAllJournalTypes(),
        mapTypes = [];

    for(var i in journalTypes) {
        var journalType = journals.getJournalType(journalTypes[i].id),
            key = args.by && args.by == "type" ? journalType.getOptions()["type"] : journalTypes[i].id;

        if (args.filter) {
            if (key.indexOf(args.filter) == -1) continue;
        } 

        mapTypes.push({ journalTypes: journalTypes[i].id, type: journalType.getOptions()["type"] });
    }

    model.mapTypes = mapTypes;

})();
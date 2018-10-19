(function() {
    var journalTypes = journals.getAllJournalTypes(),
        mapTypes = [];

    for(var i in journalTypes) {
        var journalType = journals.getJournalType(journalTypes[i].id),
            typeOption = journalType.getOptions()["type"] || "",
            key = args.by && args.by == "type" ? typeOption : journalTypes[i].id;

        if (args.filter) {
            if (key.indexOf(args.filter) == -1) continue;
        }

        mapTypes.push({ journalTypes: journalTypes[i].id, type: typeOption });
    }

    model.mapTypes = mapTypes;

})();
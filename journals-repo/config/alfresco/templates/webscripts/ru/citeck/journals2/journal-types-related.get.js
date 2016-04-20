(function() {
    var journalTypes = journals.getAllJournalTypes();

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
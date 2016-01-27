(function() {
    var allJournalTypes = services.get("journalService").getAllJournalTypes();
    var journalTypes = [];
    for(var i = 0, ii = allJournalTypes.size(); i < ii; i++) {
        journalTypes.push(allJournalTypes.get(i));
    }
    
    if(args.filter) {
        var filteredJournalTypes = [];
        for(var i in journalTypes) {
            if((journalTypes[i].id + "").indexOf(args.filter + "") != -1) {
                filteredJournalTypes.push(journalTypes[i]);
            }
        }
        journalTypes = filteredJournalTypes;
    }
    
    model.journalTypes = journalTypes;
    
})();
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
    
    model.journalTypes = journalTypes;
    
})();
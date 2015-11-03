(function() {
var journalListName = args.journalsList;

var query = 'TYPE:"journal:journalsList"';
var journalsLists = search.luceneSearch(query);

model.journalsLists = journalsLists;
})();

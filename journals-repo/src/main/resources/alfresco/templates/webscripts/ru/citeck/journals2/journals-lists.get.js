(function() {
    var journalListName = args.journalsList;

    var query = 'TYPE:"journal:journalsList"';
    var journalsLists = search.luceneSearch(query);

    var lists = [];
    var index = {};
    for(var i in journalsLists) {
        var list = journalsLists[i];
        var name = list.name;
        if(index[name]) {
            index[name].title = index[name].title || list.properties.title || null;
        } else {
            lists.push(index[name] = {
                name: name,
                title: list.properties.title || null
            });
        }
    }

    model.journalsLists = lists;
})();

(function(){

model.nodeRef = "";
if (args.user && args.user != "") {

    var user = people.getPerson(args.user);
    if(user != null) {
        var query = 'TYPE:"deputy:absenceEvent" AND @deputy\\:user_added:"' + user.nodeRef + '"';
        var nodes = search.query({
            query: query,
            language: "lucene"
        });

        if(nodes.length > 0) {
            model.nodeRef = nodes[0].nodeRef
        }
    }

}

})();
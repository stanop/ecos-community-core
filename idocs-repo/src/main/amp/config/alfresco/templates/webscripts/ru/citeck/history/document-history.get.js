(function() {
    
    var document = search.findNode(args.nodeRef);
    if(!document) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + args.nodeRef);
        return;
    }
    
    var query = 'TYPE:"history:basicEvent"';
    var mapping = {
        'events': 'event:name'
    };
    for(var name in args) {
        var values = args[name].split(',');
        var realName = mapping[name] || name;
        if (!realName.match(/[:]/)) continue;
        var escapedName = realName.replace(':', '\\:');
        query += ' AND (@' + escapedName + ':"' + values.join('" OR @' + escapedName + ':"') + '")';
    }

	query += ' AND (@event\\:document_added:\"'+args.nodeRef+'"';
	if(args.showEventForSubCases)
	{
		query += ' OR @event\\:case_added:\"'+args.nodeRef+ '")';
	}
	else
	query += ')';

    var events = search.query({
        query: query,
// sort by event:date manually, because SOLR fails with this for some reason
//        sort: [
//            {
//                column: '@event:date',
//                ascending: true
//            }
//        ]
    });
    
    model.query = query;
    
    // sort by event:date manually, because SOLR fails with this for some reason
    model.events = events.sort(function(e1, e2) {
        var d1 = e1.properties['event:date'],
            d2 = e2.properties['event:date'],
            t1 = d1 ? d1.getTime() : 0,
            t2 = d2 ? d2.getTime() : 0;
            var result;
            if(args.sort=='desc')
                result = t2 - t1;
            else
                result = t1 - t2;
        return result;
    });
})();
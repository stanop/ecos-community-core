// '@dms\\:KPP: "' + args.kpp + '"' OR @dms\\:INN: "' + args.inn + '"'
    var sysuuid = '';
    if(args.itemId != null && args.itemId != undefined && args.itemId.length > 0){
        sysuuid = ' AND NOT @sys\\:node-uuid:'+args.itemId
    }

    var nodes = search.luceneSearch('@dms\\:KPP: "' + args.kpp + '" AND @dms\\:INN: "' + args.inn + '"' + sysuuid);
	if (nodes.length == 0 || nodes == undefined) {
		status.message = "0";
	} else {
		status.message = "-1";
	}

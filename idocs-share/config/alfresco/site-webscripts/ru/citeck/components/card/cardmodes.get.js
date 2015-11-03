(function() {
	var nodeRef = page.url.args.nodeRef || null;
	var mode = page.url.args.mode || "";
    if(nodeRef) {
    	var result = remote.call("/citeck/card/modes?nodeRef=" + nodeRef);
    	if(result.status == 200) {
    		var modes = eval('(' + result + ')').cardmodes;
    	} else {
    		var modes = [];
    	}
        model.modes = modes;
    }
	model.nodeRef = nodeRef;
	model.modeId = mode;
})();

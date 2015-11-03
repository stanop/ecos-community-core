(function() {
    
    var response = remote.call('/citeck/invariants/view-check?' + url.args);
    var data = eval('(' + response + ')');
    if(response.status == 401) {
        status.code = 303;
        status.location = url.context + '/page/';
        return;
    }
    if(response.status != 200) {
        throw "Can not check view existence: " + data.message;
    }
    
    if(data.exists) {
        status.location = (url.service+'').replace(/node-edit$/, 'node-edit-page?') + url.args;
    } else if(data.defaultExists) {
        var argsCopy = [];
        for(var name in args) {
            if(name == 'viewId') continue;
            argsCopy.push(name + "=" + encodeURIComponent(args[name]));
        }
        status.location = (url.service+'').replace(/node-edit$/, 'node-edit-page?') + argsCopy.join('&');
    } else {
        status.location = (url.service+'').replace(/node-edit$/, 'edit-metadata?nodeRef=') + encodeURIComponent(args.nodeRef)
                + (args.viewId ? '&formId=' + encodeURIComponent(args.viewId) : '')
                ;
    }
    status.code = 303;
    
})();

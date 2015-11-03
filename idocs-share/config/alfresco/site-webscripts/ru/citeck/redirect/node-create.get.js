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
        status.location = (url.service+'').replace(/node-create$/, 'node-create-page?') + url.args;
    } else if(data.defaultExists) {
        var argsCopy = [];
        for(var name in args) {
            if(name == 'viewId') continue;
            argsCopy.push(name + "=" + encodeURIComponent(args[name]));
        }
        status.location = (url.service+'').replace(/node-create$/, 'node-create-page?') + argsCopy.join('&');
    } else {
        status.location = (url.service+'').replace(/node-create$/, 'create-content?itemId=') + encodeURIComponent(args.type)
                + (args.viewId ? '&formId=' + encodeURIComponent(args.viewId) : '')
                + (args.destination ? '&destination=' + encodeURIComponent(args.destination) : '')
                + (args.mimetype ? '&mimeType=' + encodeURIComponent(args.param_mimetype) : '')
                ;
    }
    status.code = 303;
    
})();

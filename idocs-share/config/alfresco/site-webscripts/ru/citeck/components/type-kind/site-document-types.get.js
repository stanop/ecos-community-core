(function() {
    var response = remote.call('/citeck/site/document-types?site=' + args.site);
    var data = eval('(' + response + ')');
    if(response.status != 200) {
        status.setCode(500, "Could not get document types: " + data.message);
        return;
    }
    
    
    
})()
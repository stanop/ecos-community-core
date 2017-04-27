function filter(array, predicate) {
    var result = [];
    for(var i in array) {
        if(predicate(array[i])) {
            result.push(array[i]);
        }
    }
    return result;
}

(function() {
    var response = remote.call('/citeck/site/document-types?site=' + args.site);
    var data = eval('(' + response + ')');
    if(response.status != 200) {
        status.setCode(500, "Could not get document types: " + data.message);
        return;
    }
    
    model.allTypes = data.types;
    model.selectedTypes = filter(data.types, function(type) {
        return type.onSite;
    })
})()
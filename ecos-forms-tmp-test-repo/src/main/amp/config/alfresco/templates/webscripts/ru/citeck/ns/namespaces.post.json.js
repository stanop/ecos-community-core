(function() {
    
    var mapping = {};
    if(json.has('uris')) {
        var uris = json.get('uris');
        for(var i = 0, ii = uris.length(); i < ii; i++) {
            var uri = uris.get(i);
            var fullQName = "{" + uri + "}test";
            var shortQName = utils.shortQName(fullQName) + "";
            var prefix = shortQName.replace(/^(.+)[:]test$/, '$1');
            mapping[prefix] = uri;
        }
    }
    if(json.has('prefixes')) {
        var prefixes = json.get('prefixes');
        for(var i = 0, ii = prefixes.length(); i < ii; i++) {
            var prefix = prefixes.get(i);
            var shortQName = prefix + ":test";
            var fullQName = utils.longQName(shortQName) + "";
            var uri = fullQName.replace(/^[{](.+)[}]test$/, '$1');
            mapping[prefix] = uri;
        }
    }
    
    model.mapping = mapping;
})()

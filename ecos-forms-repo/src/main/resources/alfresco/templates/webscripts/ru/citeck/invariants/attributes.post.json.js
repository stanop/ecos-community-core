function getAttribute(name) {
    var model = {
        name: name
    };

    var longQName = citeckUtils.createQName(name);
    
    var dictionaryService = services.get("DictionaryService");
    var propDef = dictionaryService.getProperty(longQName);
    if(propDef != null) {
        model.type = "property";
        model.datatype = utils.shortQName(propDef.dataType.name);
        if(model.datatype == "d:category") {
            model.nodetype = "cm:category";
        } else if(model.datatype == "d:noderef") {
            model.nodetype = "sys:base";
        } else {
            model.nodetype = null;
        }
        model.javaclass = propDef.dataType.javaClassName;
        return model;
    }
    
    var assocDef = dictionaryService.getAssociation(longQName);
    if(assocDef != null) {
        model.type = assocDef.isChild() ? "child-association" : "association";
        model.datatype = "d:noderef";
        model.nodetype = utils.shortQName(assocDef.targetClass.name);
        model.javaclass = "org.alfresco.service.cmr.repository.NodeRef"
        return model;
    }
    
    return null;
}

(function() {
    
    var names = json.get('names');
    var attributes = [];
    for(var i = 0, ii = names.length(); i < ii; i++) {
        var attribute = getAttribute(names.get(i));
        if(attribute != null) {
            attributes.push(attribute);
        }
    }
    
    model.names = names;
    model.attributes = attributes;
    
})();

(function() {
    
    var name = args.name;
    if(!name) {
        status.setCode(status.STATUS_BAD_REQUEST, "Parameter 'name' should be specified");
        return;
    }
    
    var longQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(name));
    
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
        return;
    }
    
    var assocDef = dictionaryService.getAssociation(longQName);
    if(assocDef != null) {
        model.type = "association";
        model.datatype = "d:noderef";
        model.nodetype = utils.shortQName(assocDef.targetClass.name);
        return;
    }
    
    status.setCode(status.STATUS_NOT_FOUND, "Can not find property or association with name '" + name + "' (" + longQName + ")");
})();

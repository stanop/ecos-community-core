(function() {
    
    var templateId = args.id;
    if(!templateId) {
        status.setCode(status.STATUS_BAD_REQUEST, "Parameter 'id' should be specified");
        return;
    }
    
    var template = enumeration.getTemplate(templateId);
    if(!template) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find template :" + templateId);
        return;
    }
    
    model.template = template;
    
})()
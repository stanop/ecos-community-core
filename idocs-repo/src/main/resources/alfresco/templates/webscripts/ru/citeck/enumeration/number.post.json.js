(function() {
    
    var templateId = args.template;
    if(!templateId) {
        status.setCode(status.STATUS_BAD_REQUEST, "Parameter 'template' should be specified");
        return;
    }
    
    var template = enumeration.getTemplate(templateId);
    if(!template) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find template :" + templateId);
        return;
    }
    
    model.template = template;
    model.number = enumeration.getNumber(template, json);
    
})()
(function() {
    
    var proto = search.findNode(args.nodeRef);
    if(!proto) {
        status.setCode(status.STATUS_NOT_FOUND, "Could not find case " + args.nodeRef);
        return;
    }
    
    if(!proto.hasAspect("icase:case")) {
        status.setCode(status.STATUS_BAD_REQUEST, "Node " + args.nodeRef + " is not a case");
        return;
    }
    
    var caseTemplateRoot = search.selectNodes("/app:company_home/app:dictionary/cm:case-templates")[0];
    if(!caseTemplateRoot) {
        status.setCode(status.STATUS_INTERNAL_ERROR, "Can not find case-templates root");
        return;
    }
    
    var template = caseTemplateRoot.createNode(null, proto.typeShort, "cm:contains");
    template.addAspect("icase:case");
    template.addAspect("icase:caseTemplate");
    caseService.copyConfiguration(proto, template);

    model.success = true;
    model.template = template;
})()

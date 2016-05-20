(function() {

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onRedirectToCreatePaymentPage",
        fn: function(record) {
            var jsNode = record.jsNode,
                nodeRef = jsNode.nodeRef;
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/contracts",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var destNodeRef = "";
                        for(i=0; i<response.json.createVariants.length; i++)
                        {
                            if(response.json.createVariants[i].type == "payments:payment")
                            {
                                destNodeRef = response.json.createVariants[i].destination;
                                break;
                            }
                        }
                        if(destNodeRef!="")
                        {
                            formId="";
                            if(jsNode.properties) {
                                if (jsNode.properties["tk:type"] == "workspace://SpacesStore/contracts-cat-doctype-contract") {
                                    formId = "by-agreement";
                                }
                            }
                            var redirection = '/share/page/node-create?type=payments:payment&contractId='+ nodeRef + '&destination=' + destNodeRef + '&formId=' + formId;
                            window.location = redirection;
                        }
                    }
                }
            });
        }
    });

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onRedirectToCreateClosingDocPage",
        fn: function(record) {
            var jsNode = record.jsNode,
                nodeRef = jsNode.nodeRef;

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "/api/journals/create-variants/site/contracts",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var destNodeRef = "";
                        for(i=0; i<response.json.createVariants.length; i++)
                        {
                            if(response.json.createVariants[i].type == "contracts:closingDocument")
                            {
                                destNodeRef = response.json.createVariants[i].destination;
                                break;
                            }
                        }
                        if(destNodeRef!="")
                        {
                            formId="";
                            if(jsNode.properties) {
                                if (jsNode.properties["tk:type"] == "workspace://SpacesStore/contracts-cat-doctype-contract") {
                                    formId = "by-agreement";
                                }
                            }
                            var redirection = '/share/page/node-create?type=contracts:closingDocument&contractId='+ nodeRef + '&destination=' + destNodeRef+'&formId='+formId;
                            window.location = redirection;

                        }
                    }
                }
            });
        }
    });
    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onRedirectToCreateSupAgreementPage",
        fn: function(record) {
            var jsNode = record.jsNode,
                nodeRef = jsNode.nodeRef;
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/contracts",
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var destNodeRef = "";
                        for(i=0; i<response.json.createVariants.length; i++)
                        {
                            if(response.json.createVariants[i].type == "contracts:supplementaryAgreement")
                            {
                                destNodeRef = response.json.createVariants[i].destination;
                                break;
                            }
                        }
                        if(destNodeRef!="")
                        {
                            formId="";
                            if(jsNode.properties) {
                                if (jsNode.properties["tk:type"] == "workspace://SpacesStore/contracts-cat-doctype-contract") {
                                    formId = "by-agreement";
                                }
                            }
                            var redirection = '/share/page/node-create?type=contracts:supplementaryAgreement&contractId='+ nodeRef + '&destination=' + destNodeRef + '&formId='+formId;
                            window.location = redirection;
                        }
                    }
                }
            });
        }
    });


})();
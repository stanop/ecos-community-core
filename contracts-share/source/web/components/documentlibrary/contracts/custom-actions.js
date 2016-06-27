(function() {

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onRedirectToCreatePaymentPage",
        fn: function(record) {
            var jsNode = record.jsNode,
                nodeRef = jsNode.nodeRef;
            var alfrescoSite = Alfresco.constants.SITE;
            var currentSite = alfrescoSite ? alfrescoSite : record.location.path.split('/')[2];

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/" + currentSite,
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
            var alfrescoSite = Alfresco.constants.SITE;
            var currentSite = alfrescoSite ? alfrescoSite : record.location.path.split('/')[2];

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "/api/journals/create-variants/site/" + currentSite,
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
            var alfrescoSite = Alfresco.constants.SITE;
            var currentSite = alfrescoSite ? alfrescoSite : record.location.path.split('/')[2];
            console.log('currentSite: ' + currentSite);
            // console.log(record);
            // console.log('alfrescoSite: ' + alfrescoSite);

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/" + currentSite,
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

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onRedirectToCreateClosingDocForInvoice",
        fn: function(record) {
            var currentSite = Alfresco.constants.SITE || record.location.path.split('/')[2];

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "/api/journals/create-variants/site/" + currentSite,
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var destNodeRef = "";
                        for(i = 0; i < response.json.createVariants.length; i++) {
                            if(response.json.createVariants[i].type == "contracts:closingDocument") {
                                destNodeRef = response.json.createVariants[i].destination;
                                break;
                            }
                        }

                        if(destNodeRef) {
                            var redirection = '/share/page/node-create?type=contracts:closingDocument&destination=' + destNodeRef;

                            if (record.jsNode.nodeRef) {
                                redirection += '&param_invoice=' + record.jsNode.nodeRef;
                            }

                            var contracts = record.jsNode.properties["payments:basis_added"];
                            if (contracts != null && contracts.length > 0) {
                                redirection += '&param_contract=' + contracts[0];
                            }

                            window.location = redirection;
                        }
                    }
                }
            });
        }
    });


})();
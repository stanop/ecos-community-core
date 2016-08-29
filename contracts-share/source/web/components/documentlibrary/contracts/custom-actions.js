(function() {

    
    var actions = { 
        onRedirectToCreatePaymentPage:  { type: "payments:payment", from: "contract" },
        onRedirectToCreateClosingDocPage: { type: "contracts:closingDocument", from: "contract" },
        onRedirectToCreateSupAgreementPage: { type: "contracts:supplementaryAgreement", from: "contract" },
        onRedirectToCreateClosingDocForInvoice: { type: "contracts:closingDocument", from: "invoice" }
    }

    for (var a in actions) {        
        YAHOO.Bubbling.fire("registerAction", { actionName: a,
            fn: function(record) {
                var jsNode = record.jsNode;

                var newWindow = window.open("", "on-redirect-from-" + actions[a].from);
                newWindow.document.body.innerHTML = "<p>" + Alfresco.util.message("label.loading") + "</p>";

                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/contracts",
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            var destNodeRef = "";

                            for(var i = 0; i < response.json.createVariants.length; i++) {
                                if(response.json.createVariants[i].type == actions[a].type) {
                                    destNodeRef = response.json.createVariants[i].destination;
                                    break;
                                }
                            }

                            if(destNodeRef) {
                                var redirection = '/share/page/node-create?type=' + actions[a].type + '&destination=' + destNodeRef;

                                switch(actions[a].from) {
                                    case "contract":
                                        var formId = jsNode.properties && jsNode.properties["tk:type"] == "workspace://SpacesStore/contracts-cat-doctype-contract" ? "by-agreement" : "";
                                        redirection += '&formId=' + formId;
                                        break;

                                    case "invoice":
                                        var contracts = jsNode.properties["payments:basis_added"];
                                        if (contracts != null && contracts.length > 0) {
                                            redirection += '&param_contract=' + contracts[0];
                                        }
                                        break;
                                }

                                if (jsNode.nodeRef) {
                                    redirection += '&param_' + actions[a].from + '=' + jsNode.nodeRef;
                                }

                                newWindow.location = redirection;
                            }
                        }
                    }                   
                });
            }
        });
    }


})();
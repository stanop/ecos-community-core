
define([
    'js/citeck/modules/utils/citeck'
], function () {

    var pageTemplatesToTest = [
        /\/share\/page\/user\/[^\/]*\/dashboard\/?$/,
        /\/share\/page\/?$/,
        /\/share\/?$/
    ];

    try {
        let forceOld = Citeck.utils.getURLParameterByName("forceOld") === 'true';

        if (forceOld) {
            return {};
        } else {
            let isAnyTemplateMatch = false;
            for (let template of pageTemplatesToTest) {
                if (template.test(window.location.pathname)) {
                    isAnyTemplateMatch = true;
                    break;
                }
            }
            if (!isAnyTemplateMatch) {
                return {};
            }
        }

        if (Citeck.newUIRedirectCheckingPerformed) {
            return {};
        }
        Citeck.newUIRedirectCheckingPerformed = true;

        Alfresco.util.Ajax.jsonGet({
            url: Alfresco.constants.PROXY_URI + 'citeck/ecos/new-ui-info-get',
            successCallback: {
                fn: function(response) {
                    if (response && response.json
                            && response.json.newUIEnabled
                            && response.json.newUIRedirectUrl) {

                        window.location.href = response.json.newUIRedirectUrl;
                    } else {
                        console.log("Strange response:", response);
                    }
                }
            },
            failureCallback: {
                fn: function(response) {
                    console.error("jsonGet failed. Response: ", response);
                }
            }
        });

    } catch (e) {
        console.error("[new-ui-redirect.js] Error", e, this);
    }

    return {};
});

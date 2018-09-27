export default function handleControl(type, payload) {
    switch (type) {

        case 'ALF_SHOW_MODAL_MAKE_UNAVAILABLE':
            return Citeck.forms.dialog("deputy:selfAbsenceEvent", "", {
                fn: function (node) {
                    handleControl("ALF_NAVIGATE_TO_PAGE", {
                        url: payload.targetUrl,
                    });
                }
            });

        case 'ALF_NAVIGATE_TO_PAGE':
            // TODO improve it
            // if (payload.targetUrlType === 'FULL_PATH')
            if (payload.target && payload.target === '_blank') {
                window.open(payload.url, '_blank');
            } else {
                window.location.href = payload.url;
            }
            break;

        case 'ALF_EDIT_SITE':
            if (Alfresco && Alfresco.module && typeof Alfresco.module.getEditSiteInstance === "function") {
                Alfresco.module.getEditSiteInstance().show({
                    shortName: payload.site
                });
            } else {
                const legacyEditSiteResource = Alfresco.constants.URL_RESCONTEXT + "modules/edit-site" + (Alfresco.constants.DEBUG ? ".js" : "-min.js");
                require([legacyEditSiteResource], function() {
                    Alfresco.module.getEditSiteInstance().show({
                        shortName: payload.site
                    });
                });
            }

            break;

        case 'ALF_DOLOGOUT':
            fetch(Alfresco.constants.URL_SERVICECONTEXT + "dologout", {
                method: "POST"
            }).then(() => {
                window.location.reload();
            });
            break;

        case 'ALF_LEAVE_SITE':
            // TODO
            // fetch(Alfresco.constants.PROXY_URI + "api/sites/test/memberships/admin", {
            //     method: "DELETE"
            // });
            break;

        default:
            console.log('Unknown control type: ', type);
    }
}
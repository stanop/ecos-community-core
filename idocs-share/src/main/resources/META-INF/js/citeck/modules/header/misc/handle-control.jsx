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
            Alfresco.module.getEditSiteInstance().show({
                shortName: payload.site
            });
            break;

        default:
            console.log('Unknown control type: ', type);
    }
}
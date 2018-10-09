import { processMenuItemsFromOldMenu, makeUserMenuItems, t } from './misc/util'


/* MODAL */
export const SHOW_MODAL = 'SHOW_MODAL';
export const HIDE_MODAL = 'HIDE_MODAL';

export function showModal(payload) {
    return {
        type: SHOW_MODAL,
        payload
    }
}

export function hideModal() {
    return {
        type: HIDE_MODAL
    }
}
/* ---------------- */



/* Create case menu */
export const CREATE_CASE_WIDGET_SET_ITEMS = 'CREATE_CASE_WIDGET_SET_ITEMS';

export function setCreateCaseWidgetItems(payload) {
    return {
        type: CREATE_CASE_WIDGET_SET_ITEMS,
        payload
    }
}
/* ---------------- */



/* User */
export const USER_SET_NAME = 'USER_SET_NAME';
export const USER_SET_FULLNAME = 'USER_SET_FULLNAME';
export const USER_SET_NODE_REF = 'USER_SET_NODE_REF';
export const USER_SET_IS_ADMIN = 'USER_SET_IS_ADMIN';
export const USER_SET_IS_AVAILABLE = 'USER_SET_IS_AVAILABLE';
export const USER_SET_PHOTO = 'USER_SET_PHOTO';


export function setUserName(payload) {
    return {
        type: USER_SET_NAME,
        payload
    }
}

export function setUserFullName(payload) {
    return {
        type: USER_SET_FULLNAME,
        payload
    }
}

export function setUserNodeRef(payload) {
    return {
        type: USER_SET_NODE_REF,
        payload
    }
}

export function setUserIsAdmin(payload) {
    return {
        type: USER_SET_IS_ADMIN,
        payload
    }
}

export function setUserIsAvailable(payload) {
    return {
        type: USER_SET_IS_AVAILABLE,
        payload
    }
}

export function setUserPhoto(payload) {
    return {
        type: USER_SET_PHOTO,
        payload
    }
}
/* ---------------- */



/* User menu */
export const USER_MENU_SET_ITEMS = 'USER_MENU_SET_ITEMS';

export function setUserMenuItems(payload) {
    return {
        type: USER_MENU_SET_ITEMS,
        payload
    }
}

export function loadUserMenuPhoto(userNodeRef) {
    return (dispatch, getState, api) => {
        if (!userNodeRef) {
            return;
        }

        api.getPhotoSize(userNodeRef).then(size => {
            if (size > 0) {
                let photoUrl = window.Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/" + userNodeRef.replace(":/", "") + "/image.jpg";
                dispatch(setUserPhoto(photoUrl));
            }
        });
    }
}
/* ---------------- */



/* Site menu */
export const SITE_MENU_SET_CURRENT_SITE_ID = 'SITE_MENU_SET_CURRENT_SITE_ID';
export const SITE_MENU_SET_SITE_MENU_ITEMS = 'SITE_MENU_SET_SITE_MENU_ITEMS';

export function setCurrentSiteId(payload) {
    return {
        type: SITE_MENU_SET_CURRENT_SITE_ID,
        payload
    }
}

export function setCurrentSiteMenuItems(payload) {
    return {
        type: SITE_MENU_SET_SITE_MENU_ITEMS,
        payload
    }
}
/* ---------------- */




/* COMMON */
export function loadTopMenuData(userName, userIsAvailable, oldMenuSiteWidgetItems) {
    return (dispatch, getState, api) => {
        let promises = [];

        const getCreateCaseMenuDataRequest = api.getCreateVariantsForAllSites().then(sites => {
            let menuItems = [];

            // TODO add it to API response
            menuItems.push(
                {
                    id: "HEADER_CREATE_WORKFLOW",
                    label: "header.create-workflow.label",
                    items: [
                        {
                            id: "HEADER_CREATE_WORKFLOW_ADHOC",
                            label: "header.create-workflow-adhoc.label",
                            targetUrl: "/share/page/start-specified-workflow?workflowId=activiti$perform"
                        },
                        {
                            id: "HEADER_CREATE_WORKFLOW_CONFIRM",
                            label: "header.create-workflow-confirm.label",
                            targetUrl: "/share/page/start-specified-workflow?workflowId=activiti$confirm"
                        },
                    ],
                },
            );

            for (let site of sites) {
                let createVariants = [];
                for (let variant of site.createVariants) {
                    // variant.isDefault
                    if (!variant.canCreate) {
                        continue;
                    }
                    createVariants.push({
                        id: "HEADER_" + ((site.siteId + "_" + variant.type).replace(/\-/g, "_")).toUpperCase(),
                        label: variant.title,
                        targetUrl: "/share/page/node-create?type=" + variant.type + "&viewId=" + variant.formId + "&destination=" + variant.destination
                    });
                }

                const siteId = "HEADER_" + (site.siteId.replace(/\-/g, "_")).toUpperCase();
                menuItems.push({
                    id: siteId,
                    label: site.siteTitle,
                    items: createVariants
                });
            }

            return menuItems;
        });

        promises.push(getCreateCaseMenuDataRequest);

        Promise.all(promises).then(([createCaseMenu]) => {
            return {
                'createCaseMenu': createCaseMenu,
                'siteMenu': processMenuItemsFromOldMenu(oldMenuSiteWidgetItems),
                'userMenu': makeUserMenuItems(userName, userIsAvailable),
            };
        }).then(result => {
            dispatch(setCreateCaseWidgetItems(result.createCaseMenu));
            dispatch(setCurrentSiteMenuItems(result.siteMenu));
            dispatch(setUserMenuItems(result.userMenu));
        });
    }
}

export function leaveSiteRequest({ site, siteTitle, user, userFullName }) {
    return (dispatch, getState, api) => {
        const url = Alfresco.constants.PROXY_URI + `api/sites/${encodeURIComponent(site)}/memberships/${encodeURIComponent(user)}`;
        return fetch(url, {
            method: "DELETE"
        }).then(resp => {
            if (resp.status !== 200) {
                return dispatch(showModal({
                    content: t("message.leave-failure", {"0": userFullName, "1": siteTitle}),
                    buttons: [
                        {
                            label: t('button.close-modal'),
                            isCloseButton: true
                        }
                    ]
                }));
            }

            dispatch(showModal({
                content: t("message.leaving", {"0": userFullName, "1": siteTitle})
            }));

            window.location.href = Alfresco.constants.URL_PAGECONTEXT + "user/" + encodeURIComponent(user) + "/dashboard";
        }).catch(err => {
            console.log('error', err);
        });
    }
}

export function joinSiteRequest({ site, siteTitle, user, userFullName }) {
    return (dispatch, getState, api) => {
        const url = Alfresco.constants.PROXY_URI + `api/sites/${encodeURIComponent(site)}/memberships`;
        const data = {
            role: "SiteConsumer",
            person: {
                userName: user
            }
        };

        return fetch(url, {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(resp => {
            if (resp.status !== 200) {
                // TODO
                console.log('joinSiteRequest err', resp);
                return;
            }

            dispatch(showModal({
                content: t("message.joining", { "0": user, "1": site })
            }));

            window.location.reload();
        }).catch(err => {
            console.log('error', err);
        });
    }
}

export function becomeSiteManagerRequest({ site, siteTitle, user, userFullName }) {
    return (dispatch, getState, api) => {
        const url = Alfresco.constants.PROXY_URI + `api/sites/${encodeURIComponent(site)}/memberships`;
        const data = {
            role: "SiteManager",
            person: {
                userName: user ? user : Alfresco.constants.USERNAME
            }
        };

        return fetch(url, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(resp => {
            if (resp.status !== 200) {
                // TODO
                console.log('becomeSiteManagerRequest err', resp);
                return;
            }

            window.location.reload();
        }).catch(err => {
            console.log('error', err);
        });
    }
}

export function requestSiteMembership({ site, siteTitle, user, userFullName }) {
    return (dispatch, getState, api) => {
        const url = Alfresco.constants.PROXY_URI + `api/sites/${encodeURIComponent(site)}/invitations`;
        const data = {
            invitationType: "MODERATED",
            inviteeRoleName: "SiteConsumer",
            inviteeUserName: user,
            inviteeComments: ""
        };

        return fetch(url, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(resp => {
            // console.log('requestSiteMembership resp', resp);
            if (resp.status !== 200) {
                const responseMessage = resp.message;
                const requestPendingMessage = "A request to join this site is in pending"; // NOTE: This is a string-literal in Share's "Site.java" file
                const failedBecausePending = responseMessage && responseMessage.indexOf(requestPendingMessage) !== -1;
                const failureMessage = failedBecausePending ? "message.request-join-site-pending-failure" : "message.request-join-site-failure";
                dispatch(showModal({
                    content: t(failureMessage)
                }));
                return;
            }

            return dispatch(showModal({
                title: t("message.request-join-success-title"),
                content: t("message.request-join-success", {"0": user, "1": siteTitle || site}),
                onCloseCallback: () => {
                    window.location.href = Alfresco.constants.URL_PAGECONTEXT + "user/" + encodeURIComponent(user) + "/dashboard";
                },
                buttons: [
                    {
                        label: t('button.leave-site.confirm-label'),
                        isCloseButton: true
                    }
                ]
            }));
        }).catch(err => {
            console.log('error', err);
        });
    }
}

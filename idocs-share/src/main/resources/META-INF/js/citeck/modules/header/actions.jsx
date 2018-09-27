import { makeSiteMenuItems, makeUserMenuItems } from './misc/util'

export const CREATE_CASE_WIDGET_SET_ITEMS = 'CREATE_CASE_WIDGET_SET_ITEMS';

export const USER_SET_NAME = 'USER_SET_NAME';
export const USER_SET_FULLNAME = 'USER_SET_FULLNAME';
export const USER_SET_NODE_REF = 'USER_SET_NODE_REF';
export const USER_SET_IS_ADMIN = 'USER_SET_IS_ADMIN';
export const USER_SET_IS_AVAILABLE = 'USER_SET_IS_AVAILABLE';
export const USER_SET_PHOTO = 'USER_SET_PHOTO';

export const SITE_MENU_SET_CURRENT_SITE_ID = 'SITE_MENU_SET_CURRENT_SITE_ID';
export const SITE_MENU_SET_CURRENT_SITE_DATA = 'SITE_MENU_SET_CURRENT_SITE_DATA';

export const USER_MENU_SET_ITEMS = 'USER_MENU_SET_ITEMS';

export function setUserMenuItems(payload) {
    return {
        type: USER_MENU_SET_ITEMS,
        payload
    }
}

export function setCreateCaseWidgetItems(payload) {
    return {
        type: CREATE_CASE_WIDGET_SET_ITEMS,
        payload
    }
}

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



export function setCurrentSiteId(payload) {
    return {
        type: SITE_MENU_SET_CURRENT_SITE_ID,
        payload
    }
}

export function setCurrentSiteData(payload) {
    return {
        type: SITE_MENU_SET_CURRENT_SITE_DATA,
        payload
    }
}

export function loadTopMenuData(siteId, userName, userIsAvailable) {
    return (dispatch, getState, api) => {
        let promises = [];

        let allSites = [];
        const getCreateCaseMenuDataRequest = api.getSitesForUser(userName).then(sites => {
            let promises = [];
            for (let site of sites) {
                allSites.push(site);
                promises.push(new Promise((resolve, reject) => {
                    api.getCreateVariantsForSite(site.shortName).then(variants => {
                        resolve(variants)
                    })
                }))
            }

            return Promise.all(promises);
        }).then(variants => {
            let menuItems = [];
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

            for (let i in allSites) {
                let createVariants = [];
                for (let variant of variants[i]) {
                    createVariants.push({
                        id: "HEADER_" + ((allSites[i].shortName + "_" + variant.type).replace(/\-/g, "_")).toUpperCase(),
                        label: variant.title,
                        targetUrl: "/share/page/node-create?type=" + variant.type + "&viewId=" + variant.formId + "&destination=" + variant.destination
                    });
                }
                const siteId = "HEADER_" + (allSites[i].shortName.replace(/\-/g, "_")).toUpperCase();
                menuItems.push({
                    id: siteId,
                    label: allSites[i].title,
                    items: createVariants
                });
            }

            return menuItems;
        });

        const getSiteDataRequest = api.getSiteData(siteId).then(result => {
            return dispatch(setCurrentSiteData({
                profile: result
            }));
        }).then(() => {
            return api.getSiteUserMembership(siteId, userName).then(result => {
                return dispatch(setCurrentSiteData({
                    userIsMember: true,
                    userIsDirectMember: !(result.isMemberOfGroup),
                    userIsSiteManager: result.role === "SiteManager"
                }));
            });
        }).then(() => {
            const state = getState();
            const user = state.user;
            const siteData = state.siteMenu;

            return makeSiteMenuItems(user, siteData);
        });

        promises.push(getCreateCaseMenuDataRequest, getSiteDataRequest);

        Promise.all(promises).then(([createCaseMenu, siteMenu]) => {
            return {
                'createCaseMenu': createCaseMenu,
                'siteMenu': siteMenu,
                'userMenu': makeUserMenuItems(userName, userIsAvailable),
            };
        }).then(result => {
            dispatch(setCreateCaseWidgetItems(result.createCaseMenu));
            dispatch(setCurrentSiteData({ items: result.siteMenu }));
            dispatch(setUserMenuItems(result.userMenu));
        });
    }
}

// TODO
// export function loadTopMenuDataTODO(siteId, userName) {
//     fetch('someUrl').then(result => {
//         dispatch(setCreateCaseWidgetItems(result.createCaseMenu));
//         dispatch(setCurrentSiteData({ items: result.siteMenu }));
//         dispatch(setUserMenuItems(result.userMenu));
//     });
// }
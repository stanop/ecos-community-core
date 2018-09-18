export const CREATE_CASE_WIDGET_SET_ITEMS = 'CREATE_CASE_WIDGET_SET_ITEMS';

export const USER_SET_NAME = 'USER_SET_NAME';
export const USER_SET_FULLNAME = 'USER_SET_FULLNAME';
export const USER_SET_NODE_REF = 'USER_SET_NODE_REF';
export const USER_SET_PHOTO = 'USER_SET_PHOTO';

export const SITE_MENU_SET_CURRENT_SITE_NAME = 'SITE_MENU_SET_CURRENT_SITE_NAME';


export function setCreateCaseWidgetItems(payload) {
    return {
        type: CREATE_CASE_WIDGET_SET_ITEMS,
        payload
    }
}

export function loadCreateCaseWidgetItems(username) {
    return (dispatch, getState, api) => {
        let allSites = [];
        api.getSitesForUser(username).then(sites => {
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
        }).then(items => {
            dispatch(setCreateCaseWidgetItems(items))
        });
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



export function setCurrentSiteName(payload) {
    return {
        type: SITE_MENU_SET_CURRENT_SITE_NAME,
        payload
    }
}

export function loadSiteData(sitename, username) {
    return (dispatch, getState, api) => {
        // TODO !!!
        api.getSiteData(sitename, username).then(result => {
            console.log('result', result);
        });
    }
}
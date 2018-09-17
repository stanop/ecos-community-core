export const CASE_MENU_SET_ITEMS = 'CASE_MENU_SET_ITEMS';

export const USER_MENU_LOAD_PHOTO = 'USER_MENU_LOAD_PHOTO';
export const USER_MENU_SET_PHOTO = 'USER_MENU_SET_PHOTO';


export function setCaseMenuItems(payload) {
    return {
        type: CASE_MENU_SET_ITEMS,
        payload
    }
}

export function setUserMenuPhoto(payload) {
    return {
        type: USER_MENU_SET_PHOTO,
        payload
    }
}



export function loadUserMenuPhoto(userNodeRef) {
    return (dispatch, getState, api) => {
        if (userNodeRef && api.getPhotoSize(userNodeRef)) {
            let photoUrl = window.Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/" + userNodeRef.replace(":/", "") + "/image.jpg";
            dispatch(setUserMenuPhoto(photoUrl));
        }
    }
}

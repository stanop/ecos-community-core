/* left menu */
export const LEFT_MENU_SET_SMALL_LOGO = 'LEFT_MENU_SET_SMALL_LOGO';
export const LEFT_MENU_SET_LARGE_LOGO = 'LEFT_MENU_SET_LARGE_LOGO';
export const LEFT_MENU_SET_ITEMS = 'LEFT_MENU_SET_ITEMS';
export const LEFT_MENU_SET_EXPANDABLE_ITEMS = 'LEFT_MENU_SET_EXPANDABLE_ITEMS';
export const LEFT_MENU_TOGGLE_EXPANDED = 'LEFT_MENU_TOGGLE_EXPANDED';
export const LEFT_MENU_TOGGLE_IS_OPEN = 'LEFT_MENU_TOGGLE_IS_OPEN';
export const LEFT_MENU_SET_SELECTED_ID = 'LEFT_MENU_SET_SELECTED_ID';
export const LEFT_MENU_SET_SCROLL_TOP = 'LEFT_MENU_SET_SCROLL_TOP';
export const LEFT_MENU_SET_IS_READY = 'LEFT_MENU_SET_IS_READY';
export const LEFT_MENU_SET_NEW_JOURNALS_PAGE_ENABLE = 'LEFT_MENU_SET_NEW_JOURNALS_PAGE_ENABLE';

export function setNewJournalsPageEnable(payload) {
    return {
        type: LEFT_MENU_SET_NEW_JOURNALS_PAGE_ENABLE,
        payload
    }
}

export function setSelectedId(payload) {
    return {
        type: LEFT_MENU_SET_SELECTED_ID,
        payload
    }
}

export function setSmallLogo(payload) {
    return {
        type: LEFT_MENU_SET_SMALL_LOGO,
        payload
    }
}

export function setLargeLogo(payload) {
    return {
        type: LEFT_MENU_SET_LARGE_LOGO,
        payload
    }
}

export function setLeftMenuItems(payload) {
    return {
        type: LEFT_MENU_SET_ITEMS,
        payload
    }
}

export function setLeftMenuExpandableItems(payload) {
    return {
        type: LEFT_MENU_SET_EXPANDABLE_ITEMS,
        payload
    }
}

export function toggleExpanded(payload) {
    return {
        type: LEFT_MENU_TOGGLE_EXPANDED,
        payload
    }
}

export function toggleIsOpen(payload) {
    return {
        type: LEFT_MENU_TOGGLE_IS_OPEN,
        payload
    }
}

export function loadMenuItemIconUrl(iconName, onSuccessCallback) {
    return (dispatch, getState, api) => {
        if (!iconName) {
            return null;
        }

        api.getMenuItemIconUrl(iconName).then(data => {
            typeof onSuccessCallback === 'function' && onSuccessCallback(data);
        });
    }
}

export function setScrollTop(payload) {
    return {
        type: LEFT_MENU_SET_SCROLL_TOP,
        payload
    }
}

export function setIsReady(payload) {
    return {
        type: LEFT_MENU_SET_IS_READY,
        payload
    }
}

/* ---------------- */

/* left menu */
export const LEFT_MENU_SET_SMALL_LOGO = 'LEFT_MENU_SET_SMALL_LOGO';
export const LEFT_MENU_SET_LARGE_LOGO = 'LEFT_MENU_SET_LARGE_LOGO';

export const LEFT_MENU_SET_ITEMS = 'LEFT_MENU_SET_ITEMS';
export const LEFT_MENU_SET_FLAT_ITEMS = 'LEFT_MENU_SET_FLAT_ITEMS';
export const LEFT_MENU_TOGGLE_EXPANDED = 'LEFT_MENU_TOGGLE_EXPANDED';

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

export function setLeftMenuFlatItems(payload) {
    return {
        type: LEFT_MENU_SET_FLAT_ITEMS,
        payload
    }
}

export function toggleExpanded(payload) {
    return {
        type: LEFT_MENU_TOGGLE_EXPANDED,
        payload
    }
}
/* ---------------- */

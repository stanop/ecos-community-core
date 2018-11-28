import { combineReducers } from 'redux';
import {
    LEFT_MENU_SET_SMALL_LOGO,
    LEFT_MENU_SET_LARGE_LOGO,

    LEFT_MENU_SET_ITEMS,
    LEFT_MENU_SET_EXPANDABLE_ITEMS,
    LEFT_MENU_TOGGLE_EXPANDED,
    LEFT_MENU_TOGGLE_IS_OPEN,
    LEFT_MENU_SET_SELECTED_ID
} from './actions';

/* leftMenuReducer */
const leftMenuInitialState = {
    smallLogo: null,
    largeLogo: null,
    selectedId: null,
    items: [],
    expandableItems: [],
    isOpen: false,
};

Object.freeze(leftMenuInitialState);

function leftMenuReducer(state = leftMenuInitialState, action) {
    switch (action.type) {
        case LEFT_MENU_SET_SELECTED_ID:
            return {
                ...state,
                selectedId: action.payload
            };

        case LEFT_MENU_SET_SMALL_LOGO:
            return {
                ...state,
                smallLogo: action.payload
            };

        case LEFT_MENU_SET_LARGE_LOGO:
            return {
                ...state,
                largeLogo: action.payload
            };

        case LEFT_MENU_SET_ITEMS:
            return {
                ...state,
                items: action.payload
            };

        case LEFT_MENU_SET_EXPANDABLE_ITEMS:
            return {
                ...state,
                expandableItems: action.payload
            };

        case LEFT_MENU_TOGGLE_IS_OPEN:
            return {
                ...state,
                isOpen: action.payload
            };

        case LEFT_MENU_TOGGLE_EXPANDED:
            return {
                ...state,
                expandableItems: (() => {
                    const expandableItem = state.expandableItems.find(fi => fi.id === action.payload);
                    const listWithoutItem = state.expandableItems.filter(fi => fi.id !== action.payload);
                    return [
                        ...listWithoutItem,
                        {
                            ...expandableItem,
                            isNestedListExpanded: !expandableItem.isNestedListExpanded
                        }
                    ];
                })(),
            };

        default:
            return state;
    }
}


/* root reducer */
export default combineReducers({
    leftMenu: leftMenuReducer
});
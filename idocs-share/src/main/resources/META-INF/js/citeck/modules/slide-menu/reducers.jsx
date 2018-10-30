import { combineReducers } from 'redux';
import {
    LEFT_MENU_SET_SMALL_LOGO,
    LEFT_MENU_SET_LARGE_LOGO,

    LEFT_MENU_SET_ITEMS,
    LEFT_MENU_SET_FLAT_ITEMS,
    LEFT_MENU_TOGGLE_EXPANDED
} from './actions';

/* leftMenuReducer */
const leftMenuInitialState = {
    smallLogo: null,
    largeLogo: null,
    selectedId: null,
    items: [],
    flatItems: [],
};

Object.freeze(leftMenuInitialState);

function leftMenuReducer(state = leftMenuInitialState, action) {
    switch (action.type) {
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

        case LEFT_MENU_SET_FLAT_ITEMS:
            return {
                ...state,
                flatItems: action.payload
            };

        case LEFT_MENU_TOGGLE_EXPANDED:
            return {
                ...state,
                flatItems: (() => {
                    const flatItem = state.flatItems.find(fi => fi.id === action.payload);
                    const listWithoutItem = state.flatItems.filter(fi => fi.id !== action.payload);
                    return [
                        ...listWithoutItem,
                        {
                            ...flatItem,
                            isNestedListExpanded: !flatItem.isNestedListExpanded
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
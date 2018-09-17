import { combineReducers } from 'redux';
import { CASE_MENU_SET_ITEMS, USER_MENU_SET_PHOTO } from './actions';

/* caseMenuReducer */
const caseMenuInitialState = {
    items: [],
};

Object.freeze(caseMenuInitialState);

function caseMenuReducer(state = caseMenuInitialState, action) {
    switch (action.type) {
        case CASE_MENU_SET_ITEMS:
            return {
                ...state,
                items: action.payload
            };

        default:
            return state;
    }
}

/* userMenuReducer */
const userMenuInitialState = {
    userPhoto: '',
};

Object.freeze(userMenuInitialState);

function userMenuReducer(state = userMenuInitialState, action) {
    switch (action.type) {
        case USER_MENU_SET_PHOTO:
            return {
                ...state,
                userPhoto: action.payload
            };

        default:
            return state;
    }
}


/* root reducer */
export default combineReducers({
    caseMenu: caseMenuReducer,
    userMenu: userMenuReducer,
});
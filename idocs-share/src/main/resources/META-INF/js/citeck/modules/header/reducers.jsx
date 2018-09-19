import { combineReducers } from 'redux';
import {
    CREATE_CASE_WIDGET_SET_ITEMS,

    USER_SET_PHOTO,
    USER_SET_NAME,
    USER_SET_FULLNAME,
    USER_SET_NODE_REF,
    USER_SET_IS_ADMIN,

    SITE_MENU_SET_CURRENT_SITE_ID,
    SITE_MENU_SET_CURRENT_SITE_DATA, USER_SET_IS_AVAILABLE
} from './actions';

/* caseMenuReducer */
const caseMenuInitialState = {
    items: [],
};

Object.freeze(caseMenuInitialState);

function caseMenuReducer(state = caseMenuInitialState, action) {
    switch (action.type) {
        case CREATE_CASE_WIDGET_SET_ITEMS:
            return {
                ...state,
                items: action.payload
            };

        default:
            return state;
    }
}

/* userReducer */
const userInitialState = {
    name: '',
    fullName: '',
    nodeRef: '',
    photo: '',
    isAdmin: false,
    isAvailable: false
};

Object.freeze(userInitialState);

function userReducer(state = userInitialState, action) {
    switch (action.type) {
        case USER_SET_NAME:
            return {
                ...state,
                name: action.payload
            };

        case USER_SET_FULLNAME:
            return {
                ...state,
                fullName: action.payload
            };

        case USER_SET_NODE_REF:
            return {
                ...state,
                nodeRef: action.payload
            };

        case USER_SET_PHOTO:
            return {
                ...state,
                photo: action.payload
            };

        case USER_SET_IS_ADMIN:
            return {
                ...state,
                isAdmin: action.payload
            };

        case USER_SET_IS_AVAILABLE:
            return {
                ...state,
                isAvailable: action.payload
            };

        default:
            return state;
    }
}


/* siteMenuReducer */
const siteMenuInitialState = {
    id: '',
    profile: {
        title: "",
        shortName: "",
        visibility: "PRIVATE",
    },

    userIsSiteManager: false,
    userIsMember: false,
    userIsDirectMember: false,

    items: []
};

Object.freeze(siteMenuInitialState);

function siteMenuReducer(state = siteMenuInitialState, action) {
    switch (action.type) {
        case SITE_MENU_SET_CURRENT_SITE_ID:
            return {
                ...state,
                id: action.payload
            };

        case SITE_MENU_SET_CURRENT_SITE_DATA:
            return {
                ...state,
                ...action.payload
            };

        default:
            return state;
    }
}


/* root reducer */
export default combineReducers({
    caseMenu: caseMenuReducer,
    siteMenu: siteMenuReducer,
    user: userReducer
});
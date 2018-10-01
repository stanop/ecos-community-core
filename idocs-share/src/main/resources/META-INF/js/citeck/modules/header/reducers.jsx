import { combineReducers } from 'redux';
import {
    CREATE_CASE_WIDGET_SET_ITEMS,

    USER_SET_PHOTO,
    USER_SET_NAME,
    USER_SET_FULLNAME,
    USER_SET_NODE_REF,
    USER_SET_IS_ADMIN,
    USER_SET_IS_AVAILABLE,

    SITE_MENU_SET_CURRENT_SITE_ID,
    SITE_MENU_SET_SITE_MENU_ITEMS,

    USER_MENU_SET_ITEMS,

    SHOW_MODAL,
    HIDE_MODAL
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

/* userMenuReducer */
const userMenuInitialState = {
    items: [],
};

Object.freeze(userMenuInitialState);

function userMenuReducer(state = userMenuInitialState, action) {
    switch (action.type) {
        case USER_MENU_SET_ITEMS:
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
    siteId: '',
    items: []
};

Object.freeze(siteMenuInitialState);

function siteMenuReducer(state = siteMenuInitialState, action) {
    switch (action.type) {
        case SITE_MENU_SET_CURRENT_SITE_ID:
            return {
                ...state,
                siteId: action.payload
            };

        case SITE_MENU_SET_SITE_MENU_ITEMS:
            return {
                ...state,
                items: action.payload
            };

        default:
            return state;
    }
}


/* modalReducer */
const modalInitialState = {
    isOpen: false,
    title: "",
    content: "",
    onCloseCallback: null,
    buttons: []
};

/*
Button list example:
buttons: [
    {
        label: 'Button label',
        onClick: () => {
            ...some actions
        },
        bsStyle: "primary",
        isCloseButton: false
    },
    ...other buttons
]
 */

Object.freeze(modalInitialState);

function modalReducer(state = modalInitialState, action) {
    switch (action.type) {
        case SHOW_MODAL:
            return {
                ...state,
                isOpen: true,
                ...action.payload
            };

        case HIDE_MODAL:
            return modalInitialState;

        default:
            return state;
    }
}


/* root reducer */
export default combineReducers({
    caseMenu: caseMenuReducer,
    siteMenu: siteMenuReducer,
    modal: modalReducer,
    userMenu: userMenuReducer,
    user: userReducer
});
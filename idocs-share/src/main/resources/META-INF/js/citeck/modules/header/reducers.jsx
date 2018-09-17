import { combineReducers } from 'redux';
import {CREATE_CASE_WIDGET_SET_ITEMS, USER_SET_PHOTO, USER_SET_NAME, USER_SET_FULLNAME, USER_SET_NODE_REF} from './actions';

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

        default:
            return state;
    }
}


/* root reducer */
export default combineReducers({
    caseMenu: caseMenuReducer,
    user: userReducer,
});
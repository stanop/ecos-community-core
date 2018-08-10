import {
    SET_CARD_MODE,
    SET_PAGE_ARGS,
    REQUEST_CARDLETS,
    RECEIVE_CARDLETS,
    REQUEST_CONTROL,
    RECEIVE_CONTROL,
    REQUEST_NODE_BASE_INFO,
    RECEIVE_NODE_BASE_INFO,
    CARDLET_LOADED
} from './actions';

let reducersStore = {

    [SET_CARD_MODE]: function (state = {}, action) {
        return {
            ...state,
            visitedCardModes: {
                ...state.visitedModes,
                [action.cardMode]: true
            },
            currentCardMode: action.cardMode
        }
    },

    [SET_PAGE_ARGS]: function (state = {}, action) {
        return {
            ...state,
            pageArgs: action.pageArgs
        };
    },

    [REQUEST_CARDLETS]: function (state = {}, action) {
        return {
            ...state,
            cardletsData: {
                isFetching: true
            }
        };
    },
    [RECEIVE_CARDLETS]: function (state = {}, action) {
        return {
            ...state,
            cardletsData: {
                isFetching: false,
                receivedAt: action.receivedAt,
                cardlets: action.data.cardlets,
                cardModes: action.data.cardModes
            }
        };
    },
    [REQUEST_CONTROL]: function (state = {}, action) {
        let controls = state.controls || {};
        return {
            ...state,
            controls: {
                ...controls,
                [action.control.url]: {
                    isFetching: true
                }
            }
        };
    },
    [RECEIVE_CONTROL]: function (state = {}, action) {
        let controls = state.controls || {};
        return {
            ...state,
            controls: {
                ...controls,
                [action.control.url]: {
                    isFetching: false,
                    data: action.data
                }
            }
        };
    },
    [REQUEST_NODE_BASE_INFO]: function (state = {}, action) {
        let nodes = state.nodes || {};
        let nodeInfo = nodes[action.nodeRef] || {};
        return {
            ...state,
            nodes: {
                ...nodes,
                [action.nodeRef]: {
                    ...nodeInfo,
                    baseInfo: {
                        isFetching: true
                    }
                }
            }
        }
    },
    [RECEIVE_NODE_BASE_INFO]: function (state = {}, action) {
        let nodes = state.nodes || {};
        let nodeInfo = nodes[action.nodeRef] || {};
        return {
            ...state,
            nodes: {
                ...nodes,
                [action.nodeRef]: {
                    ...nodeInfo,
                    baseInfo: action.data
                }
            }
        }
    },
    [CARDLET_LOADED]: function (state = {}, action) {
        let cardletsData = state.cardletsData || {};
        let loadingState = cardletsData.loadingState || {};
        if (loadingState[action.cardletId]) {
            return state;
        } else {
            return {
                ...state,
                cardletsData: {
                    ...cardletsData,
                    loadingState: {
                        ...loadingState,
                        [action.cardletId]: true
                    }
                }
            }
        }
    }
};

export const rootReducer = function (state = {}, action) {
    let reducer = reducersStore[action.type];
    return reducer ? reducer(state, action) : state;
};

export const registerReducers = function (reducers) {
    for (let actionId in reducers) {
        if (reducers.hasOwnProperty(actionId)) {
            reducersStore[actionId] = reducers[actionId];
        }
    }
};

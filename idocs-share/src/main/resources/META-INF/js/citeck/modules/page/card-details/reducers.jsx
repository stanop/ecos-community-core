import {
    SET_CARD_MODE,
    SET_PAGE_ARGS,
    REQUEST_CARDLETS,
    RECEIVE_CARDLETS,
    REQUEST_CONTROL,
    RECEIVE_CONTROL,
    REQUEST_NODE_BASE_INFO,
    RECEIVE_NODE_BASE_INFO,
    REQUEST_CARDLET_DATA,
    RECEIVE_CARDLET_DATA,
    RECEIVE_ERR_CARDLET_DATA
} from './actions';

let reducersStore = {

    [SET_CARD_MODE]: function (state = {}, action) {
        let visitedModes = state.visitedCardModes || {};
        return {
            ...state,
            visitedCardModes: {
                ...visitedModes,
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
        let dataBefore = state.cardletsData || {};
        return {
            ...state,
            cardletsData: {
                ...dataBefore,
                isFetching: true
            }
        };
    },
    [RECEIVE_CARDLETS]: function (state = {}, action) {
        let dataBefore = state.cardletsData || {};
        return {
            ...state,
            cardletsData: {
                ...dataBefore,
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
        let baseInfo = nodeInfo.baseInfo || {};
        return {
            ...state,
            nodes: {
                ...nodes,
                [action.nodeRef]: {
                    ...nodeInfo,
                    baseInfo: {
                        ...baseInfo,
                        isFetching: true
                    }
                }
            }
        }
    },
    [RECEIVE_NODE_BASE_INFO]: function (state = {}, action) {
        let nodes = state.nodes || {};
        let nodeInfo = nodes[action.nodeRef] || {};
        if (nodeInfo.modified !== action.data.modified) {
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
        } else {
            return state;
        }
    },
    [REQUEST_CARDLET_DATA]: function (state = {}, action) {
        let cardletsFetchedData = state.cardletsFetchedData || {};
        let cardletData = cardletsFetchedData[action.cardlet.id] || {};
        return {
            ...state,
            cardletsFetchedData: {
                ...cardletsFetchedData,
                [action.cardlet.id]: {
                    ...cardletData,
                    isFetching: true
                }
            }
        }
    },
    [RECEIVE_CARDLET_DATA]: function (state = {}, action) {
        let cardletsFetchedData = state.cardletsFetchedData || {};
        let cardletData = cardletsFetchedData[action.cardlet.id] || {};
        return {
            ...state,
            cardletsFetchedData: {
                ...cardletsFetchedData,
                [action.cardlet.id]: {
                    ...cardletData,
                    isFetching: false,
                    data: action.data
                }
            },
            cardModesLoading: updateModesLoading(state, state.cardModesLoading, action.cardlet)
        }
    },
    [RECEIVE_ERR_CARDLET_DATA]: function (state = {}, action) {
        let cardletsFetchedData = state.cardletsFetchedData || {};
        let cardletData = cardletsFetchedData[action.cardlet.id] || {};
        return {
            ...state,
            cardletsFetchedData: {
                ...cardletsFetchedData,
                [action.cardlet.id]: {
                    ...cardletData,
                    isFetching: false,
                    error: action.error
                }
            },
            cardModesLoading: updateModesLoading(state, state.cardModesLoading, action.cardlet)
        }
    }
    /*[CARDLET_LOADED]: function (state = {}, action) {
        let cardletsData = state.cardletsData || {};
        let loadingState = cardletsData.loadingState || {};
        if (loadingState[action.cardletId]) {
            return state;
        } else {
            let date = new Date();
            console.log("[" + date + "] cardlet " + action.cardletId + " loaded after " + (window.__CARD_DETAILS_START - date.getTime()) + " ms.");
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
    }*/
};

function updateCardletLoadingState(cardletsLoadingState = {}, modesLoading = {}, cardlet) {
    if (!modesLoading[cardlet.cardMode]) {
        let cardletsLoadingState = Object.assign({}, cardletsLoadingState);
        let isModeLoaded = function (modeId) {
            let cardlets = state.cardletsData.cardlets;
            for (let cardlet of cardlets) {
                if (cardlet.cardMode === modeId) {
                    if (!cardletsLoadingState[cardlet.id]) {
                        return false;
                    }
                }
            }
            return true;
        };
        if (isModeLoaded(cardlet.cardMode)) {
            return {
                ...modesLoading,
                [cardlet.cardMode]: true
            };
        }
    }
    return modesLoading;
}

/*
function getModesLoadedState(cardletsData = {}) {
    let cardletsLoadingState = cardletsData.loadingState || {};
    let isCardModeLoaded = (modeId) => {
        let cardlets = cartdletsData.cardlets;
        for (var i = 0; i < cardlets.length; i++) {
            if (!cardletsLoadingState[cardlets[i].id]) {
                return false;
            }
        }
    };
    let result = {};
    for (let mode of cardletsData.cardModes) {
        result[mode.id] = isCardModeLoaded(mode.id);
    }
    return result;
}*/

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

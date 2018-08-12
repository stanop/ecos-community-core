
export const SET_PAGE_ARGS = 'SET_PAGE_ARGS';
export const SET_CARD_MODE = 'SET_CARD_MODE';

export const REQUEST_CARDLETS = 'REQUEST_CARDLETS';
export const RECEIVE_CARDLETS = 'RECEIVE_CARDLETS';

export const REQUEST_CONTROL = 'REQUEST_CONTROLLER';
export const RECEIVE_CONTROL = 'RECEIVE_CONTROLLER';

export const REQUEST_NODE_BASE_INFO = 'REQUEST_NODE_BASE_INFO';
export const RECEIVE_NODE_BASE_INFO = 'RECEIVE_NODE_BASE_INFO';

export const CARDLET_LOADED = 'CARDLET_LOADED';

export function setCardMode(cardMode, registerReducers) {

    return (dispatch, getState) => {

        let state = getState();

        if (state.currentCardMode === cardMode) {
            return;
        }

        let visitedBefore = ((state.visitedCardModes || {})[cardMode]) === true;

        if (!visitedBefore) {
            let cardlets = state.cardletsData.cardlets;
            for (let c of cardlets) {
                let cardletMode = c.cardMode || "default";
                if (cardletMode === cardMode) {
                    dispatch(fetchCardletControl(c.control, registerReducers));
                }
            }
        }

        dispatch({
            type: SET_CARD_MODE,
            cardMode: cardMode
        });
    }
}

export function setPageArgs(pageArgs) {
    return {
        type: SET_PAGE_ARGS,
        pageArgs: pageArgs
    }
}

export function fetchNodeBaseInfo(nodeRef) {

    return (dispatch, getState) => {

        let getCurrentInfo = function () {
            return ((getState().nodes || {})[nodeRef] || {}).baseInfo || {};
        };

        let info = getCurrentInfo();

        if (info.isFetching) {
            return Promise.resolve();
        }

        dispatch({
            type: REQUEST_NODE_BASE_INFO,
            nodeRef: nodeRef
        });

        return fetch("/share/proxy/alfresco/citeck/node/base-info?nodeRef=" + nodeRef)
            .then(response => response.json())
            .then(json => {
                dispatch({
                    type: RECEIVE_NODE_BASE_INFO,
                    nodeRef: nodeRef,
                    data: json
                });

                if (json.pendingUpdate) {
                    setTimeout(() => {
                        dispatch(fetchNodeBaseInfo(nodeRef)).then(() => {
                            info = getCurrentInfo();
                            if (!info.pendingUpdate) {
                                dispatch(fetchCardlets(nodeRef));
                                YAHOO.Bubbling.fire('metadataRefresh');
                            }
                        })
                    }, 2000);
                }
            })
    }
}

export function fetchCardlets(nodeRef) {

    return dispatch => {

        dispatch({
            type: REQUEST_CARDLETS,
            nodeRef: nodeRef
        });

        return fetch("/share/proxy/alfresco/citeck/card/cardlets?mode=all&nodeRef=" + nodeRef, {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            dispatch({
                type: RECEIVE_CARDLETS,
                receivedAt: Date.now(),
                data: json
            });
        });
    }
}

export function fetchCardletControl(control, registerReducers) {

    return (dispatch, getState) => {

        let state = getState();
        let existing = (state.controls || {})[control.url];

        if (existing) {
            return Promise.resolve();
        }

        dispatch({
            type: REQUEST_CONTROL,
            control
        });

        return new Promise((resolve, reject) => {
            require([control.url], function(data) {
                if (data.reducers) {
                    registerReducers(data.reducers);
                }
                dispatch({
                    type: RECEIVE_CONTROL,
                    data,
                    control
                });
                resolve(data);
            });
        });
    }
}

export function cardletLoaded(cardletId) {
    return {
        type: CARDLET_LOADED,
        cardletId
    }
}

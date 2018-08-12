
export const SET_PAGE_ARGS = 'SET_PAGE_ARGS';
export const SET_CARD_MODE = 'SET_CARD_MODE';

export const REQUEST_CARDLETS = 'REQUEST_CARDLETS';
export const RECEIVE_CARDLETS = 'RECEIVE_CARDLETS';

export const REQUEST_CONTROL = 'REQUEST_CONTROLLER';
export const RECEIVE_CONTROL = 'RECEIVE_CONTROLLER';

export const REQUEST_NODE_BASE_INFO = 'REQUEST_NODE_BASE_INFO';
export const RECEIVE_NODE_BASE_INFO = 'RECEIVE_NODE_BASE_INFO';

export const REQUEST_CARDLET_DATA = 'REQUEST_CARDLET_DATA';
export const RECEIVE_CARDLET_DATA = 'RECEIVE_CARDLET_DATA';
export const RECEIVE_ERR_CARDLET_DATA = 'RECEIVE_ERR_CARDLET_DATA';

export const CARD_MODE_LOADED = 'CARD_MODE_LOADED';

export function setCardMode(cardMode, registerReducers) {

    return (dispatch, getState) => {

        let state = getState();

        if (state.currentCardMode === cardMode) {
            return;
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

export function fetchCardletData(cardlet) {

    return (dispatch, getState) => {

        let state = getState();
        let control = (state.controls || {})[cardlet.control.url];

        if (!control) {

            dispatch({
                type: REQUEST_CONTROL,
                control
            });

            require([cardlet.control.url], function(data) {
                let controlClass = data.default;
                dispatch({
                    type: RECEIVE_CONTROL,
                    controlClass,
                    control
                });
            });

            control = (state.controls || {})[cardlet.control.url];
        }

        if (control.isFetching) {
            require([cardlet.control.url], function () {
                dispatch(fetchCardletData(cardlet));
            });
            return;
        }

        let controlClass = control.controlClass;

        let cardletsFetchedData = state.cardletsFetchedData || {};
        let cardletData = cardletsFetchedData[cardlet.id] || {};
        let nodeState = state.nodes[state.pageArgs.nodeRef] || {};

        let fetchKey = controlClass.constructor.getFetchKey(nodeState, cardletData);

        if (fetchKey != null) {

            if (cardletData.fetchKey !== fetchKey) {

                dispatch({
                    type: REQUEST_CARDLET_DATA,
                    cardlet,
                    fetchKey
                });

                controlClass.constructor.fetchData(nodeState, cardletData, data => {
                    dispatch({
                        type: RECEIVE_CARDLET_DATA,
                        fetchKey,
                        cardlet,
                        data
                    })
                }, error => {
                    console.error(error);
                    dispatch({
                        type: RECEIVE_ERR_CARDLET_DATA,
                        fetchKey,
                        cardlet,
                        error
                    })
                })
            }
        }
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

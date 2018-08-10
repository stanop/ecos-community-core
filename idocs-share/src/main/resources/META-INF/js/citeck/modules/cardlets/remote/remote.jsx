import React from 'react';
import { connect } from 'react-redux';
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

const REQUEST_REMOTE_CONTROL = "REQUEST_REMOTE_CONTROL";
const RECEIVE_REMOTE_HTML = "RECEIVE_REMOTE_HTML";
const REMOTE_CONTROL_LOADED = "REMOTE_CONTROL_LOADED";
const REMOTE_CONTROL_ERROR = "REMOTE_CONTROL_ERROR";

const RemoteCardlet = function (props) {

    props.onInitialized();

    return <div dangerouslySetInnerHTML={{__html: props.text}} />;
};

function updateControl(state = {}, url, data) {
    let remoteControls = state.remoteControls || {};
    let currentData = remoteControls[url] || {};
    return {
        ...state,
        remoteControls: {
            ...remoteControls,
            [url]: {
                ...currentData,
                ...data
            }
        }
    }
}

export const reducers = {

    [REQUEST_REMOTE_CONTROL]: function(state = {}, action) {
        return updateControl(state, action.url, {
            isFetching: true
        });
    },
    [RECEIVE_REMOTE_HTML]: function (state = {}, action) {
        return updateControl(state, action.url, {
            isFetching: false,
            text: action.text
        });
    },
    [REMOTE_CONTROL_LOADED]: function (state = {}, action) {
        return state;
    }
};

const mapStateToProps = (state = {}, ownProps) => {

    let reqParams = Object.assign({}, ownProps.props);
    let remoteUrl = reqParams.remoteUrl;
    delete reqParams.remoteUrl;

    let fullUrl = remoteUrl + '?' + $.param(reqParams);

    return {
        fullUrl
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {

    return {
        onInitialized: () => dispatch((dispatch, getState) => {

            let reqParams = Object.assign({}, ownProps.props);
            let remoteUrl = reqParams.remoteUrl;
            delete reqParams.remoteUrl;

            let fullUrl = remoteUrl + '?' + $.param(reqParams);

            let state = getState();
            if ((state.remoteControls || {})[fullUrl]) {
                return;
            }

            dispatch({
                type: REQUEST_REMOTE_CONTROL,
                url: fullUrl
            });

            CiteckUtils.loadHtml(
                fullUrl, null,
                text => {
                    dispatch({
                        type: RECEIVE_REMOTE_HTML,
                        url: fullUrl,
                        text
                    })
                },
                function () {
                    dispatch({
                        type: REMOTE_CONTROL_LOADED,
                        url: fullUrl
                    });
                    ownProps.onLoaded();
                },
                function () {
                    dispatch({
                        type: REMOTE_CONTROL_ERROR,
                        url: fullUrl
                    });
                    ownProps.onLoaded();
                }
            );
        })
    }
};

export const control = connect(
    mapStateToProps,
    mapDispatchToProps
)(RemoteCardlet);


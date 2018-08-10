import React from 'react';
import { connect } from 'react-redux';
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

const REQUEST_REMOTE_CONTROL = "REQUEST_REMOTE_CONTROL";
const RECEIVE_REMOTE_HTML = "RECEIVE_REMOTE_HTML";
const REMOTE_CONTROL_LOADED = "REMOTE_CONTROL_LOADED";
const REMOTE_CONTROL_ERROR = "REMOTE_CONTROL_ERROR";

const RemoteCardlet = function (props) {

    props.onInitialized();

    return <div dangerouslySetInnerHTML={{__html: props.htmlText}} />;
};

function updateControl(state = {}, remoteId, data) {
    let remoteControls = state.remoteControls || {};
    let currentData = remoteControls[remoteId] || {};
    return {
        ...state,
        remoteControls: {
            ...remoteControls,
            [remoteId]: {
                ...currentData,
                ...data
            }
        }
    }
}

export const reducers = {

    [REQUEST_REMOTE_CONTROL]: function(state = {}, action) {
        return updateControl(state, action.remoteId, {
            isFetching: true
        });
    },
    [RECEIVE_REMOTE_HTML]: function (state = {}, action) {
        return updateControl(state, action.remoteId, {
            isFetching: false,
            htmlText: action.htmlText
        });
    },
    [REMOTE_CONTROL_LOADED]: function (state = {}, action) {
        return state;
    }
};

const mapStateToProps = (state = {}, ownProps) => {

    let controls = state.remoteControls || {};
    let htmlText = (controls[ownProps.props.remoteId] || {}).htmlText || '';

    return {
        htmlText
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {

    return {
        onInitialized: () => dispatch((dispatch, getState) => {

            let reqParams = Object.assign({}, ownProps.props);
            let remoteUrl = reqParams.remoteUrl;
            let remoteId = reqParams.remoteId;

            delete reqParams.remoteUrl;
            delete reqParams.remoteId;

            let fullUrl = remoteUrl + '?' + $.param(reqParams);

            let state = getState();
            if ((state.remoteControls || {})[remoteId]) {
                return;
            }

            dispatch({
                type: REQUEST_REMOTE_CONTROL,
                remoteId
            });

            CiteckUtils.loadHtml(
                fullUrl, null,
                htmlText => {
                    dispatch({
                        type: RECEIVE_REMOTE_HTML,
                        remoteId,
                        htmlText
                    })
                },
                function () {
                    dispatch({
                        type: REMOTE_CONTROL_LOADED,
                        remoteId
                    });
                    ownProps.onLoaded();
                },
                function () {
                    dispatch({
                        type: REMOTE_CONTROL_ERROR,
                        remoteId
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


import React from 'react';
import { connect } from 'react-redux';
import { NodeCardlet } from '../node-cardlet';
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

export default class RemoteCardlet extends NodeCardlet {

    static getFetchKey(nodeState) {
        return nodeState.baseInfo.nodeRef;
    }

    static fetchData(nodeState, ownProps, onSuccess, onFailure) {

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
    }

    static mapStateToProps(nodeState, cardletState) {
        return {
            htmlText: (cardletState.data || {}).htmlText || ''
        };
    };

    render() {
        return <div dangerouslySetInnerHTML={{__html: props.htmlText}} />;
    }
};



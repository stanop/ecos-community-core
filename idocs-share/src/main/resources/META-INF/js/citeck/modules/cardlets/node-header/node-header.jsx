import React from 'react';
import { connect } from 'react-redux';

//require('xstyle!./case-status.css');

const REQUEST_NODE_HEADER_INFO = "REQUEST_NODE_HEADER_INFO";
const RECEIVE_NODE_HEADER_INFO = "RECEIVE_NODE_HEADER_INFO";

const NodeHeader = function (props) {

    if (props.info.modified != props.nodeModified) {
        CaseStatus.fetchData();
    }

    props.onLoaded();

    return <div id="cardlet-case-status" className="case-status document-details-panel">
        <h2 className="alfresco-twister">
            <div className="status-line-el">Статус:</div>
            <div className={`panel-body case-status-name ${loadingClass} status-line-el`}>{statusName}</div>
        </h2>
    </div>;
};

function updateNodeHeaderState(state = {}, nodeRef, stateUpdate) {
    let nodesHeaderInfo = state.nodesHeaderInfo || {};
    let nodeHeaderInfo = nodesHeaderInfo[nodeRef];
    return {
        ...state,
        nodesHeaderInfo: {
            ...nodesHeaderInfo,
            [nodeRef]: {
                ...nodeHeaderInfo,
                ...stateUpdate
            }
        }
    }
}

export const reducers = {
    [REQUEST_NODE_HEADER_INFO]: (state, action) => {
        return updateNodeHeaderState(state, action.nodeRef, {
            isFetching: true,
            modified: action.modified
        });
    },
    [RECEIVE_NODE_HEADER_INFO]: (state = {}, action) => {
        return updateNodeHeaderState(state, action.nodeRef, {
            isFetching: false,
            data: action.data
        });
    }
};

const mapStateToProps = (state, ownProps) => {
    let nodeRef = ownProps.nodeRef;
    let info = (state.nodesHeaderInfo || {})[nodeRef] || {};
    let currentModified = state.nodeInfo.modified;
    return {
        info: info,
        nodeModified: currentModified
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {

    return {

        fetchData: () => dispatch((dispatch, getState) => {

            let nodeRef = ownProps.nodeRef;

            let state = getState();

            let currentModified = ownProps.nodeInfo.modified;
            let nodeData = state.nodes[nodeRef];
            /*if (nodeData.headerInfo )*//*if (nodeData.headerInfo )*/



            let statuses = state.caseStatuses || {};
            let statusState = statuses[nodeRef] || {};

            let modifiedInStore = statusState.modified || 0;

            if (modifiedInStore === ownProps.nodeInfo.modified) {
                return;
            }

            dispatch({
                type: REQUEST_CASE_STATUS,
                nodeRef,
                modified: ownProps.nodeInfo.modified
            });

            return fetch('/share/proxy/alfresco/citeck/case/status?nodeRef=' + nodeRef, {
                credentials: 'include'
            }).then(response => {
                return response.json();
            }).then(data => {
                dispatch({
                    type: RECEIVE_CASE_STATUS,
                    nodeRef,
                    data
                });
            });
        })
    }
};

export const control = connect(
    mapStateToProps,
    mapDispatchToProps
)(CaseStatus);

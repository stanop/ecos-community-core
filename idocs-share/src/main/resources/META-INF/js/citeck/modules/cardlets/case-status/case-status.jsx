import React from 'react';
import { connect } from 'react-redux';

require('xstyle!./case-status.css');

const REQUEST_CASE_STATUS = "REQUEST_CASE_STATUS";
const RECEIVE_CASE_STATUS = "RECEIVE_CASE_STATUS";

const CaseStatus = function (props) {

    props.onInitialize();

    let status = props.status;

    let loadingClass = status.isFetching !== false ? 'loading' : '';

    let statusName = "";
    if (!status.isFetching) {
        statusName = status.data.statusName;
    }

    props.onLoaded();

    return <div id="cardlet-case-status" className="case-status document-details-panel">
        <h2 className="alfresco-twister">
            <div className="status-line-el">Статус:</div>
            <div className={`panel-body case-status-name ${loadingClass} status-line-el`}>{statusName}</div>
        </h2>
    </div>;
};

function updateStatusState(state = {}, nodeRef, statusState) {
    let statuses = state.caseStatuses || {};
    let statusStateBefore = statuses[nodeRef] || {};
    return {
        ...state,
        caseStatuses: {
            ...statuses,
            [nodeRef]: {
                ...statusStateBefore,
                ...statusState
            }
        }
    }
}

export const reducers = {
    [REQUEST_CASE_STATUS]: (state, action) => {
        return updateStatusState(state, action.nodeRef, {
            isFetching: true,
            modified: action.modified
        });
    },
    [RECEIVE_CASE_STATUS]: (state = {}, action) => {
        return updateStatusState(state, action.nodeRef, {
            isFetching: false,
            data: action.data
        });
    }
};

const mapStateToProps = (state, ownProps) => {

    let nodeRef = ownProps.nodeRef;

    let caseStatus;
    if (ownProps.nodeInfo.pendingUpdate) {
        caseStatus = {
            isFetching: true,
            modified: ownProps.nodeInfo.modified
        }
    } else {
        let caseStatuses = state ? state.caseStatuses : null;
        caseStatus = caseStatuses ? caseStatuses[nodeRef] : {
            isFetching: true,
            modified: ownProps.nodeInfo.modified,
        };
    }

    return {
        nodeRef: nodeRef,
        status: caseStatus
    }
};

const mapDispatchToProps = (dispatch, ownProps) => {

    return {

        onInitialize: () => dispatch((dispatch, getState) => {

            let nodeRef = ownProps.nodeRef;
            let state = getState();

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

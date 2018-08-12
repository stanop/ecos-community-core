import React from "react";
import { NodeCardlet } from '../node-cardlet'

import 'xstyle!./case-status.css';

export default class CaseStatus extends NodeCardlet {

    static mapStateToProps(state, ownProps) {

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
    }

    static getFetchUrl(nodeState, ownProps) {
        return '/share/proxy/alfresco/citeck/case/status?nodeRef=' + ownProps.nodeRef;
    }

    render() {

        let props = this.props;
        let status = props.status;

        let loadingClass = status.isFetching !== false ? 'loading' : '';

        let statusName = "";
        if (!status.isFetching) {
            statusName = status.data.statusName || 'Без статуса';
        }

        props.onLoaded();

        return <div id="cardlet-case-status" className="case-status document-details-panel">
            <h2 className="alfresco-twister">
                <div className="status-line-el">Статус:</div>
                <div className={`panel-body case-status-name ${loadingClass} status-line-el`}>{statusName}</div>
            </h2>
        </div>;
    }
}
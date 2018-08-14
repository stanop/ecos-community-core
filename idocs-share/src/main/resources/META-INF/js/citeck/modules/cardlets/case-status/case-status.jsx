import React from "react";
import NodeCardlet from '../node-cardlet';

import 'xstyle!./case-status.css';

export default class CaseStatus extends NodeCardlet {

    static getFetchUrl(ownProps) {
        return '/share/proxy/alfresco/citeck/case/status?nodeRef=' + ownProps.nodeRef;
    }

    render() {

        let props = this.props;
        let status = this.props.data;

        let loadingClass = props.isFetching !== false ? 'loading' : '';

        let statusName = "";
        if (!props.isFetching) {
            statusName = status.statusName || 'Без статуса';
        }

        return <div id="cardlet-case-status" className="case-status document-details-panel">
            <h2 className="alfresco-twister">
                <div className="status-line-el">Статус:</div>
                <div className={`panel-body case-status-name ${loadingClass} status-line-el`}>{statusName}</div>
            </h2>
        </div>;
    }
}
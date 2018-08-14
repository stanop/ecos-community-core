import React from 'react';

export default class NodeCardlet extends React.Component {

    static getFetchKey(ownProps) {
        if (ownProps.nodeInfo.pendingUpdate) {
            return null;
        } else {
            return ownProps.nodeInfo.modified;
        }
    }

    static fetchData(ownProps, onSuccess, onFailure) {
        let getFetchUrl = this.prototype.constructor.getFetchUrl;
        if (getFetchUrl) {
            let url = getFetchUrl(ownProps);
            fetch(url, { credentials: 'include' })
                .then(response => { return response.json();})
                .then(onSuccess)
                .catch(onFailure);
        } else {
            onFailure("getFetchUrl is not implemented");
        }
    }
}
import React from 'ecosui!react';

export default class NodeCardlet extends React.Component {

    static getFetchKey(ownProps) {
        return ownProps.nodeInfo.modified;
    }

    static fetchData(ownProps, onSuccess, onFailure) {
        let getFetchUrl = this.prototype.constructor.getFetchUrl;
        if (getFetchUrl) {
            let url = getFetchUrl(ownProps);
            fetch(url, { credentials: 'include' })
                .then(response => { return response.json();})
                .then(data => {
                    onSuccess({
                        ...data,
                        nodePendingUpdate: ownProps.nodeInfo.pendingUpdate
                    })
                })
                .catch(onFailure);
        } else {
            onFailure("getFetchUrl is not implemented");
        }
    }
}
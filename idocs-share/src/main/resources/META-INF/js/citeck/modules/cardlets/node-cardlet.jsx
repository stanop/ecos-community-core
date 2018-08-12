import { React } from 'react';

export default class NodeCardlet extends React.Component {

    static getFetchKey(nodeState, ownProps) {
        if (nodeState.baseInfo.pendingUpdate) {
            return null;
        } else {
            return nodeState.baseInfo.modified;
        }
    }

    static fetchData(nodeState, ownProps, onSuccess, onFailure) {
        if (this.constructor.getFetchUrl) {
            let url = this.constructor.getFetchUrl(nodeState, ownProps);
            fetch(url, { credentials: 'include' })
                .then(response => { return response.json();})
                .then(onSuccess)
                .catch(onFailure);
        } else {
            onFailure("getFetchUrl is not implemented");
        }
    }
}
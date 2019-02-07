import React from 'react';
import NodeCardlet from '../node-cardlet';

export default class ImgPreview extends NodeCardlet {

    static getFetchKey(ownProps) {
        return ownProps.nodeRef;
    }

    static fetchData(ownProps, onSuccess, onFailure) {

        let htmlId = 'card-details-cardlet_' + ownProps.id + "_imgpreview";

        onSuccess({
            controlProps: ownProps.controlProps,
            htmlId: htmlId,
            nodeRef: ownProps.nodeRef,
            modified: ownProps.nodeInfo.modified
        });
    }

    componentDidMount() {

        let controlProps = this.props.data.controlProps;
        let htmlId = this.props.data.htmlId;

        if (!controlProps.hideTwister) {
            let twisterKey = controlProps.twisterKey || 'dc';
            Alfresco.util.createTwister(`${htmlId}-heading`, twisterKey);
        }
    }

    render() {

        let htmlId = this.props.data.htmlId;
        let headerId = this.props.data.controlProps.header;
        let imgSrc = this.props.data.controlProps.imgSrc;
        let modified = this.props.data.modified;

        let header = Alfresco.messages.global[headerId] || headerId;

        return <div id={`${htmlId}-panel`} className='document-details-panel'>
            <h2 id={`${htmlId}-heading`} className="thin dark">
                {header}
                <span id={`${htmlId}-heading-actions`} className="alfresco-twister-actions" style={{position: 'relative', float: 'right'}}/>
            </h2>
            <div className="panel-body">
                <div id={`${htmlId}`} className="document-view">
                    <div id={`${htmlId}-view`} className="document-view">
                        <img style={{
                            'display': 'block',
                            'marginLeft': 'auto',
                            'marginRight': 'auto',
                            'maxWidth': '100%'
                        }} src={imgSrc + '&modified=' + modified} />
                    </div>
                </div>
            </div>
        </div>
    }
}

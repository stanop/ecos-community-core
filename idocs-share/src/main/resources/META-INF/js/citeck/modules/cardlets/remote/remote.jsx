import React from 'react';
import { connect } from 'react-redux';
import NodeCardlet from '../node-cardlet';
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

export default class RemoteCardlet extends NodeCardlet {

    static getFetchKey(ownProps) {
        return ownProps.nodeRef;
    }

    static fetchData(ownProps, onSuccess, onFailure) {

        let reqParams = Object.assign({}, ownProps.controlProps);
        let remoteUrl = reqParams.remoteUrl;

        delete reqParams.remoteUrl;
        delete reqParams.remoteId;

        let fullUrl = remoteUrl + '?' + $.param(reqParams);

        let data = {
            id: ownProps.id
        };

        CiteckUtils.loadHtml({
            url: fullUrl,
            htmlDest: text => {
                data['htmlText'] = text;
            },
            jsInlineDest: inlineScripts => {
                data['jsInlineScripts'] = inlineScripts;
            },
            onFailure: () => {
                console.error("error");
                onFailure(arguments);
            },
            onSuccess: () => onSuccess(data)
        });
    }

    componentDidMount() {
        let scripts = this.props.data.jsInlineScripts;
        for (let i = 0; i < scripts.length; i++) {
            eval(scripts[i]);
        }
    }

    render() {
        let html = this.props.data.htmlText;
        let htmlId = "cardlet-remote-" + this.props.data.id;
        return <div id={htmlId} dangerouslySetInnerHTML={{__html: html}} />;
    }
};


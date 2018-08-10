import React from "react";

import SurfRegion from "../../surf/surf-region";
import ShareFooter from "../../footer/share-footer";
import { connect } from 'react-redux';

import {
    setCardMode,
    cardletLoaded
} from "./actions";

export default function CardDetails(props) {

    let pageArgs = props.pageArgs;

    let createUploaderRegion = function (id) {
        return <SurfRegion key={`uploader-${id}`} args={{
            regionId: id,
            scope: 'template',
            templateId: "card-details",
            cacheAge: 1000
        }}/>
    };

    return <div>
            <div key="card-details-body" className="sticky-wrapper">
                <div id="doc3">
                    <div id="alf-hd">
                        <SurfRegion args={{
                            regionId: "share-header",
                            scope: "global",
                            chromeless: "true",
                            pageid: "card-details",
                            site: pageArgs.site,
                            theme: pageArgs.theme,
                            cacheAge: 300,
                            userName: props.userName
                        }} />
                    </div>
                    <div id="bd">
                        <CardletsBodyView {...props} />
                        {[
                            createUploaderRegion('html-upload'),
                            createUploaderRegion('flash-upload'),
                            createUploaderRegion('dnd-upload'),
                            createUploaderRegion('archive-and-download'),
                            createUploaderRegion('file-upload')
                        ]}
                    </div>
                </div>
                <div className="sticky-push" />
            </div>
            <ShareFooter key="card-details-footer" className="sticky-footer" theme={pageArgs.theme} />
        </div>;
}

/*======CARDLETS_BODY=======*/

function CardletsBody(props) {

    let modes = props.modes;
    let cardlets = props.cardlets;

    return <div>
        {cardlets['all']['top']}
        <div id="card-details-tabs" className="header-tabs">
            {modes.map(mode => {
                return <CardletsModeTabView key={`card-mode-link-${mode.id}`} {...mode} />
            })}
        </div>
        <div>
            {modes.map(mode => {
                return <CardletsModeBodyView key={`card-mode-body-${mode.id}`} {...mode} cardlets={cardlets[mode.id]} />
            })}
        </div>
    </div>;
}

const cardletsBodyMapProps = state => {

    let cardlets = {
        'all': {'top': []}
    };

    for (let cardlet of state.cardletsData.cardlets) {
        if (cardlet.id === 'card-modes') {
            continue;
        }
        let mode;
        if (cardlet.column !== 'top' || cardlet.order > 'm5') {
            mode = cardlet.cardMode || 'default';
        } else {
            mode = 'all'
        }
        let columns = cardlets[mode];
        if (!columns) {
            columns = {
                top: [],
                left: [],
                right: [],
                bottom: []
            };
            cardlets[mode] = columns;
        }
        let column = columns[cardlet.column];
        if (column) {
            column.push(cardlet);
        }
    }

    let loadingState = (state.cardletsData || {}).loadingState || {};

    let isCardletsLoaded = function (modeCardlets) {
        if (!modeCardlets) {
            return true;
        }
        for (let columnId in modeCardlets) {
            if (modeCardlets.hasOwnProperty(columnId)) {
                for (let cardlet of modeCardlets[columnId]) {
                    if (!loadingState[cardlet.id]) {
                        return false;
                    }
                }
            }
        }
        return true;
    };

    let modes = [
        {
            "id": "default",
            "title": Alfresco.util.message('card.mode.default.title'),
            "description": Alfresco.util.message('card.mode.default.description')
        },
        ...state.cardletsData.cardModes
    ].map(mode => {
        return {
            ...mode,
            isActive: mode.id === state.currentCardMode,
            loaded: isCardletsLoaded(cardlets[mode.id])
        }
    });

    return {
        cardlets: cardlets,
        modes
    };
};

const CardletsBodyView = connect(cardletsBodyMapProps)(CardletsBody);

/*======CARDLETS_BODY=======*/

/*====CARDLETS_MODE_TAB=====*/

function CardletsModeTab(props) {

    let className = "header-tab";

    if (props.isActive) {
        className += " current";
    }

    return <span className={className}>
        <a onClick={props.onClick}>{props.title}</a>
    </span>
}

const CardletsModeTabView = connect(
    (state, ownProps) => ownProps,
    (dispatch, ownProps) => {
        return {
            onClick: () => dispatch(setCardMode(ownProps.id))
        }
    }
)(CardletsModeTab);

/*====CARDLETS_MODE_TAB=====*/

/*====CARDLETS_MODE_BODY====*/

function CardletsModeBody (props) {

    let className = "card-mode-body";
    if (!props.isActive) {
        className += " hidden";
    }

    let cardlets = props.cardlets || {};

    let contentClass = props.loaded ? 'active' : 'not-active';
    let loadingClass = props.loaded ? 'not-active' : 'active';

    let createCardlets = cardlets => (cardlets || []).map(data => <CardletView {...data} />);

    return <div id={`card-mode-${props.id}`} className={className}>
        <div className={`card-details-mode-body ${loadingClass} loading-overlay`}>
            <div className="loading-container">
                <div className="loading-indicator" />
            </div>
        </div>
        <div className={`card-details-mode-body ${contentClass}`}>
            {createCardlets(cardlets['top'])}
            <div className="yui-gc">
                <div className="yui-u first">
                    {createCardlets(cardlets['left'])}
                </div>
                <div className="yui-u">
                    {createCardlets(cardlets['right'])}
                </div>
            </div>
            {createCardlets(cardlets['bottom'])}
        </div>
    </div>
}

const CardletsModeBodyView = CardletsModeBody;

/*====CARDLETS_MODE_BODY====*/

/*=========CARDLET==========*/

function Cardlet(props) {

    return <div className='cardlet'
                data-available-in-mobile={ props.mobileOrder > -1 }
                data-position-index-in-mobile={ props.mobileOrder }>
        <props.control {...props} />
    </div>;
}

const evalExpRegexp = /\${((?:(?!\${)[\S\s])+?)}/g;

const cardletMapProps = (state, ownProps) => {

    let nodeRef = state.pageArgs.nodeRef;

    let props = (ownProps || {}).control.props || {};
    let convertedProps = {};

    for (let prop in props) {
        convertedProps[prop] = props[prop].replace(evalExpRegexp, function (match, expr) {
            try {
                return eval(expr);
            } catch (e) {
                console.error(e);
                return expr;
            }
        });
    }

    let controlData = (state.controls || {})[ownProps.control.url];
    let control;
    if (controlData && controlData.data) {
        control = controlData.data.control;
    } else {
        control = "div";
    }

    return {
        control: control,
        props: convertedProps,
        nodeRef: nodeRef,
        modified: state.nodes[nodeRef].baseInfo.modified
    };
};

const cardletMapDispatch = (dispatch, ownProps) => {
    return {
        onLoaded: () => dispatch(cardletLoaded(ownProps.id))
    }
};

const CardletView = connect(cardletMapProps, cardletMapDispatch)(Cardlet);

/*=========CARDLET==========*/
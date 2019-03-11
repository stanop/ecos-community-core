import React from "react";
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

import SurfRegion from "../../surf/surf-region";
import ShareFooter from "../../footer/share-footer";
import { connect } from 'react-redux';

import {
    setCardMode,
    fetchCardletData,
    setStartMessage
} from "./actions";

function CardDetailsImpl(props) {

    let pageArgs = props.pageArgs;

    let createUploaderRegion = function (id) {
        return <SurfRegion key={`uploader-${id}`} args={{
            regionId: id,
            scope: 'template',
            templateId: "card-details",
            cacheAge: 1000
        }}/>
    };

    const headerComponent = <SurfRegion args={{
        regionId: "share-header",
        scope: "global",
        chromeless: "true",
        pageid: "card-details",
        site: pageArgs.site,
        theme: pageArgs.theme,
        cacheAge: 300,
        userName: props.userName
    }} />;

    let uploadersComponents = [];

    if (props.anyCardModeLoaded) {
        uploadersComponents = [
            createUploaderRegion('dnd-upload'),
            createUploaderRegion('file-upload')
        ];
    }

    return (
        <div id='card-details-container'>
            <div key="card-details-body" className="sticky-wrapper">
                <div id="doc3">
                    <div id="alf-hd">
                        {headerComponent}
                        <StartMessage />
                    </div>
                    <div id="bd">
                        <TopPanel />
                        <CardletsBodyView {...props} />
                    </div>
                    <div id="card-details-uploaders" style={{display: 'none'}}>
                        {uploadersComponents}
                    </div>
                </div>
                <div className="sticky-push" />
            </div>
            <ShareFooter key="card-details-footer" className="sticky-footer" theme={pageArgs.theme} />
        </div>
    );
}

const CardDetails = connect((state, ownProps) => {
    return {
        ...ownProps,
        anyCardModeLoaded: !!((state.modesLoadingState || {})['any'])
    }
})(CardDetailsImpl);

export default CardDetails;

/*======START_MESSAGE=======*/

const StartMessageComponent = ({ text, closeMessage }) => {
    if (!text) {
        return null;
    }

    return (
        <div className='card-details-start-message'>
            {Alfresco.util.message(text)}
            <span className='card-details-start-message-close' onClick={closeMessage} />
        </div>
    );
};

const StartMessage = connect(
    state => ({
        text: state.startMessage
    }),
    (dispatch, ownProps) => ({
        closeMessage: () => dispatch(setStartMessage(''))
    }),
)(StartMessageComponent);


/*======TopPanel=======*/

const PROCESS_MODEL_NODE_TYPE = 'ecosbpm:processModel';
const EDITOR_PAGE_CONTEXT = '/share/page/bpmn-editor/';
const DESIGNER_PAGE_CONTEXT = '/share/page/bpmn-designer';

class TopPanelComponent extends React.Component {
    render() {
        const { nodeInfo, nodeRef } = this.props;
        const nodeType = nodeInfo.nodeType;

        const permissions = nodeInfo.permissions;

        const buttons = [];
        switch (nodeType) {
            case PROCESS_MODEL_NODE_TYPE:
                const recordId = nodeRef.replace('workspace://SpacesStore/', '');
                buttons.push({
                    className: 'ecos-button ecos-button_blue back-to-list-button',
                    href: DESIGNER_PAGE_CONTEXT,
                    text: Alfresco.util.message('bpmn-card.go-back-to-list-button.text')
                });
                if (permissions.Write) {
                    buttons.push({
                        className: 'ecos-button ecos-button_blue open-editor-button',
                        href: `${EDITOR_PAGE_CONTEXT}#/editor/${recordId}`,
                        text: Alfresco.util.message('bpmn-card.open-editor-button.text')
                    });
                }
                break;
            default:
                break;
        }

        if (buttons.length < 1) {
            return null;
        }

        return (
            <div className={'card-details-top-panel'}>
                {buttons.map((button, idx) => {
                    return (
                        <a key={idx} className={button.className} href={button.href}>
                            {button.text}
                        </a>
                    );
                })}
            </div>
        );
    }
}

const TopPanel = connect(
    (state, ownProps) => {
        let nodeRef = state.pageArgs.nodeRef;
        return {
            nodeRef,
            nodeInfo: state.nodes[nodeRef]
        };
    }
)(TopPanelComponent);

/*======CARDLETS_BODY=======*/

function CardletsBody(props) {

    let modes = props.modes;
    let cardlets = props.cardlets;

    return <div className="cardlets-body">
        {createCardlets(cardlets['all']['top'])}
        <div id="card-details-tabs" className="header-tabs">
            {modes.map(mode => {
                return <CardletsModeTabView key={`card-mode-link-${mode.id}`} {...mode} />
            })}
        </div>
        <div>
            {modes.map(mode => {
                return <CardletsModeBodyView key={`card-mode-body-${mode.id}`}
                                             {...mode}
                                             cardlets={cardlets[mode.id]} />
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
        let mode = cardlet.cardMode;
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

    let modesLoadingState = state.modesLoadingState || {};
    let visitedCardModes = state.visitedCardModes || {};

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
            visited: !!visitedCardModes[mode.id],
            loaded: !!modesLoadingState[mode.id]
        }
    });

    return {
        cardlets,
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
            onClick: () => {
                CiteckUtils.setURLParameter("mode", ownProps.id);
                dispatch(setCardMode(ownProps.id))
            }
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

    let cardlets = props.visited ? (props.cardlets || {}) : {
        top: [],
        left: [],
        right: [],
        bottom: []
    };

    let isCardletsEmpty = function(cardlets) {
        for (let space in cardlets) {
            if (cardlets.hasOwnProperty(space) && cardlets[space].length) {
                return false;
            }
        }
        return true;
    };

    let loaded = isCardletsEmpty(cardlets) || props.loaded;

    let contentClass = loaded ? 'active' : 'not-active';
    let loadingClass = loaded ? 'not-active' : 'active';

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

const Cardlet = function (props) {

    props.fetchData(props);

    let loaded = props.modeLoaded && props.cardletState.data && props.controlClass;

    if (!loaded) {
        return <div/>
    } else {
        return <div className='cardlet'
                    data-available-in-mobile={props.mobileOrder > -1}
                    data-position-index-in-mobile={props.mobileOrder}>
            <props.controlClass {...props.cardletState} />
        </div>;
    }
};

const evalExpRegexp = /\${((?:(?!\${)[\S\s])+?)}/g;

const cardletMapProps = (state, ownProps) => {

    let nodeRef = state.pageArgs.nodeRef;
    let theme = state.pageArgs.theme;

    let rawProps = ownProps.control.props || {};
    let controlProps = {};

    for (let prop in rawProps) {
        controlProps[prop] = rawProps[prop].replace(evalExpRegexp, (match, expr) => {
            if (expr === 'nodeRef') {
                return nodeRef;
            } else if (expr === 'theme') {
                return theme;
            }
            try {
                return eval(expr);
            } catch (e) {
                console.error(e);
                return expr;
            }
        });
    }
    let modesLoadingState = state.modesLoadingState || {};
    let cardletState = (state.cardletsState || {})[ownProps.id] || {};
    let controlClass = ((state.controls || {})[ownProps.control.url] || {}).controlClass;

    return {
        ...ownProps,
        modeLoaded: ownProps.cardMode == 'all' || !!modesLoadingState[ownProps.cardMode],
        cardletState,
        controlProps,
        controlClass,
        nodeRef,
        nodeInfo: state.nodes[nodeRef]
    };
};

const cardletMapDispatch = (dispatch) => {
    return {
        fetchData: (props) => dispatch(fetchCardletData(props))
    }
};

const CardletView = connect(cardletMapProps, cardletMapDispatch)(Cardlet);
const createCardlets = cardlets => (cardlets || []).map(data => <CardletView {...data} />);

/*=========CARDLET==========*/
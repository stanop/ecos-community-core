import React from "react";
import ReactDOM from "react-dom";
import SurfRegion from "../../surf/surf-region";
import $ from "jquery";
import ShareFooter from "../../footer/share-footer";
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';

import "xstyle!citeck/components/card/card-details.css";

function CardDetails(props) {

    let pageArgs = props.pageArgs;

    let createUploaderRegion = function (id) {
        return <SurfRegion key={`uploader-${id}`} args={{
            regionId: id,
            scope: 'template',
            templateId: "card-details",
            cacheAge: 1000
        }}/>
    };

    return [
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
                    <CardletsBody {...props} />
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
        </div>,
        <ShareFooter key="card-details-footer" className="sticky-footer" theme={pageArgs.theme} />
    ];
}

export function renderPage (elementId, props) {
    ReactDOM.render(React.createElement(CardDetails, props), document.getElementById(elementId));
}

class CardletsBody extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            data: null,
            cardmodes: null,
            activeMode: null,
            nodeBaseInfo: props.nodeBaseInfo,
            visitedModes: {}
        };

        let self = this;

        window.onpopstate = function() {
            let urlMode = CiteckUtils.getURLParameterByName("mode");
            self.setState({
                activeMode: urlMode ? urlMode : "default"
            });
        };

        YAHOO.Bubbling.on('metadataRefresh', this._updateNodeBaseInfo, this);

        this.handleCardletInitialized = this.handleCardletInitialized.bind(this);
    }

    handleCardletInitialized(cardlet) {
        let loadingState = CardletsBody.prototype.cardletsLoadingState;
        if (!loadingState) {
            loadingState = {};
            CardletsBody.prototype.cardletsLoadingState = loadingState;
        }
        loadingState[cardlet.props.regionId] = true;

        this.forceUpdate();
    }

    componentDidMount() {

        let initialMode = CiteckUtils.getURLParameterByName("mode") || "default";
        let newState = {
            activeMode: initialMode,
            visitedModes: {}
        };
        newState.visitedModes[initialMode] = true;

        this.setState(newState);
        this._updateCardlets();
    }

    _updateNodeBaseInfo() {
        let requestArgs = {
            nodeRef: this.props.pageArgs.nodeRef
        };
        fetch(this.props.alfescoUrl + "citeck/node/base-info?" + $.param(requestArgs), {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            if (this.state.nodeBaseInfo.modified != json.modified) {
                this.setState({
                    nodeBaseInfo: json
                });
                this._updateCardlets();
            }
        });
    }

    _updateCardlets() {

        let pageArgs = this.props.pageArgs;
        let requestArgs = {
            'nodeRef': pageArgs.nodeRef,
            'mode': 'all',
            'cb': this.state.nodeBaseInfo.modified,
            'withModes': true
        };
        fetch(this.props.alfescoUrl + "citeck/card/cardlets?" + $.param(requestArgs), {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            let cardlets = {
                'all': {'top': []}
            };
            for (let cardlet of json.cardlets) {
                if (cardlet.regionId == 'card-modes') {
                    continue;
                }
                let mode;
                if (cardlet.regionColumn != 'top' || cardlet.regionPosition > 'm5') {
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
                let column = columns[cardlet.regionColumn];
                if (column) {
                    column.push(this.getCardletInstance(cardlet));
                }
            }
            this.setState({
                data: json,
                cardlets: cardlets,
                cardmodes: [
                    {
                        "id": "default",
                        "title": Alfresco.util.message('card.mode.default.title'),
                        "description": Alfresco.util.message('card.mode.default.description')
                    },
                    ...json.cardModes
                ]
            });
        });
    }

    getCardletInstance(props) {

        let key = `${props.regionId}-${props.regionColumn}-${props.regionPosition}`;

        let instances = CardletsBody.prototype.cardlets;
        if (!instances) {
            instances = {};
            CardletsBody.prototype.cardlets = instances;
        }
        let result = instances[key];
        if (!result) {
            result = <Cardlet key={key}
                              pageArgs={this.props.pageArgs}
                              onInitialized={this.handleCardletInitialized}
                              {...props} />;
            instances[key] = result;
        }

        return result;
    }

    render() {

        if (!this.state.data || !this.state.cardmodes) {
            return '';
        }

        let self = this;
        let modes = this.state.cardmodes;

        let onModeTabClick = function (modeId) {
            CiteckUtils.setURLParameter("mode", modeId);
            let newState = {
                activeMode: modeId
            };
            if (!self.state.visitedModes[modeId]) {
                newState.visitedModes = Object.assign({}, self.state.visitedModes);
                newState.visitedModes[modeId] = true;
            }
            self.setState(newState);
        };

        let loadingState = CardletsBody.prototype.cardletsLoadingState || {};

        let isCardletsLoaded = function (cardlets) {
            for (let column in  cardlets) {
                if (cardlets.hasOwnProperty(column)) {
                    for (let cardlet of cardlets[column]) {
                        if (!loadingState[cardlet.props.regionId]) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        let cardlets = this.state.cardlets;

        return <div>
            {cardlets['all']['top']}
            <div id="card-details-tabs" className="header-tabs">
                {modes.map(mode => {
                    return <CardletModeTab key={`card-mode-link-${mode.id}`}
                                           modeId={mode.id}
                                           isActive={mode.id == self.state.activeMode}
                                           title={mode.title}
                                           onClick={onModeTabClick}/>
                })}
            </div>
            <div>
                {modes.map(mode => {
                    let modeCardlets = self.state.visitedModes[mode.id] ? cardlets[mode.id] : {};
                    return <CardletModeBody key={`card-mode-body-${mode.id}`}
                                            modeId={mode.id}
                                            cardlets={modeCardlets}
                                            isActive={mode.id == self.state.activeMode}
                                            loaded={isCardletsLoaded(modeCardlets)} />
                })}
            </div>
        </div>;
    }
}

function CardletModeTab(props) {

    let className = "header-tab";

    if (props.isActive) {
        className += " current";
    }

    return <span className={className}>
        <a onClick={() => props.onClick(props.modeId)}>{props.title}</a>
    </span>
}

function CardletModeBody (props) {

    let className = "card-mode-body";
    if (!props.isActive) {
        className += " hidden";
    }

    let cardlets = props.cardlets;

    let contentClass = props.loaded ? 'active' : 'not-active';
    let loadingClass = props.loaded ? 'not-active' : 'active';

    return <div id={`card-mode-${props.modeId}`} className={className}>
        <div className={`card-details-mode-body ${loadingClass} loading-overlay`}>
            <div className="loading-container">
                <div className="loading-indicator" />
            </div>
        </div>
        <div className={`card-details-mode-body ${contentClass}`}>
            {cardlets['top'] || []}
            <div className="yui-gc">
                <div className="yui-u first">
                    {cardlets['left'] || []}
                </div>
                <div className="yui-u">
                    {cardlets['right'] || []}
                </div>
            </div>
            {cardlets['bottom'] || []}
        </div>
    </div>
}

export class Cardlet extends React.Component {

    constructor(props) {
        super(props);

        this.handleInitialized = this.handleInitialized.bind(this);

        if (props.component) {
            this.state = {
                component: ''
            };
        } else {
            let regionArgs = {
                regionId: props.regionId,
                scope: 'page'
            };
            regionArgs = Object.assign(regionArgs, props.pageArgs);
            let region = <SurfRegion autoInit={false} args={ regionArgs } onInitialized={this.handleInitialized} />;
            this.state = {
                component: region
            };
        }
    }

    handleInitialized() {
        if (this.props.onInitialized != null) {
            this.props.onInitialized(this);
        }
    }

    componentDidMount() {

        let self = this;
        let componentId = this.props.component;

        if (componentId) {

            require([componentId], function(component) {
                self.setState({
                    component: <component.default {...self.props} />
                });
            });
        }
    }

    render() {
        return <div className='cardlet'
                    data-available-in-mobile={ this.props.availableInMobile }
                    data-position-index-in-mobile={ this.props.positionIndexInMobile }>
            { this.state.component }
        </div>;
    }
}

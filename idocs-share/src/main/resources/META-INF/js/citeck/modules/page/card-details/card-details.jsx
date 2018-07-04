import "js/citeck/lib/fetch";
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
                        cacheAge: 300
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
            activeMode: "default"
        };

        let self = this;

        window.onpopstate = function() {
            let urlMode = CiteckUtils.getURLParameterByName("mode");
            self.setState({
                activeMode: urlMode ? urlMode : "default"
            });
        }
    }

    componentDidMount() {

        let initialMode = CiteckUtils.getURLParameterByName("mode");
        if (initialMode) {
            this.setState({
                activeMode: initialMode
            });
        }

        let pageArgs = this.props.pageArgs;
        let requestArgs = {
            'nodeRef': pageArgs.nodeRef,
            'mode': 'all'
        };
        fetch(this.props.alfescoUrl + "citeck/card/cardlets?" + $.param(requestArgs), {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            let cardlets = {};
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
                cardlets: cardlets
            });
        });

        requestArgs = {
            nodeRef: this.props.pageArgs.nodeRef
        };
        fetch(this.props.alfescoUrl + "citeck/card/modes?" + $.param(requestArgs), {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            this.setState({
                cardmodes: [{
                        "id": "default",
                        "title": Alfresco.util.message('card.mode.default.title'),
                        "description": Alfresco.util.message('card.mode.default.description')
                    },
                    ...json.cardmodes
                ]
            });
        });
    }

    getCardletInstance(props) {

        let key = `${props.regionId}-${props.regionColumn}-${props.regionPosition}`;

        let instances = CardDetails.prototype.cardlets;
        if (!instances) {
            instances = {};
            CardDetails.prototype.cardlets = instances;
        }
        let result = instances[key];
        if (!result) {
            result = <Cardlet key={key} pageArgs={this.props.pageArgs} {...props} />;
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
            self.setState({
                activeMode: modeId
            });
        };

        const cardlets = this.state.cardlets;

        let tabsData = modes.map(mode => {
            let isActive = mode.id == self.state.activeMode;
            return {
                link: <CardletModeTab key={`link-${mode.id}`}
                                      modeId={mode.id}
                                      isActive={isActive}
                                      title={mode.title}
                                      onClick={onModeTabClick} />,
                body: <CardletModeBody key={`route-${mode.id}`}
                                       modeId={mode.id}
                                       cardlets={cardlets[mode.id]}
                                       isActive={isActive} />
            }
        });

        return <div>
            {cardlets['all']['top']}
            <div id="card-details-tabs" className="header-tabs">
                {tabsData.map(tab => tab.link)}
            </div>
            <div>
                {tabsData.map(tab => tab.body)}
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

class CardletModeBody extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            initialized: false,
            cardlets: {
                left: [],
                right: [],
                bottom: [],
                top: []
            }
        }
    }

    static getDerivedStateFromProps(props, state) {
        if (state.initialized == false && props.isActive) {
            return {
                initialized: true,
                cardlets: props.cardlets
            }
        }
        return null;
    }

    render() {

        let className = "card-mode-body";
        if (!this.props.isActive) {
            className += " hidden";
        }

        let cardlets = this.state.cardlets;
        return <div id={`card-mode-${this.props.modeId}`} className={className}>
            {cardlets['top']}
            <div className="yui-gc">
                <div className="yui-u first">
                    {cardlets['left']}
                </div>
                <div className="yui-u">
                    {cardlets['right']}
                </div>
            </div>
            {cardlets['bottom']}
        </div>
    }
}

export class Cardlet extends React.Component {

    constructor(props) {
        super(props);
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
            this.state = {
                component: <SurfRegion args={ regionArgs } />
            };
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

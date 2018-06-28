import "js/citeck/lib/fetch";
import React from "react";
import ReactDOM from "react-dom";
import SurfRegion from "../../surf/surf-region";
import $ from "jquery";
import ShareFooter from "../../footer/share-footer";

import "xstyle!citeck/components/card/card-details.css"

export function CardDetails(props) {

    let pageArgs = props.pageArgs;

    let createUploaderRegion = function (id) {
        return <SurfRegion args={{
            regionId: id,
            scope: 'template',
            templateId: "card-details",
            cacheAge: 1000
        }}/>
    };

    return [
        <div className="sticky-wrapper">
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
        <ShareFooter className="sticky-footer" theme={pageArgs.theme} />
    ];
}

export function renderPage (elementId, props) {
    ReactDOM.render(React.createElement(CardDetails, props), document.getElementById(elementId));
}

class CardletsBody extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            data: null
        };
    }

    componentDidMount() {
        let pageArgs = this.props.pageArgs;
        let requestArgs = [
            'nodeRef=' + pageArgs.nodeRef
        ];
        if (pageArgs.mode) {
            requestArgs.push('mode=' + pageArgs.mode);
        }
        let url = this.props.alfescoUrl + "citeck/card/cardlets?" + requestArgs.join('&');
        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            this.setState({data: json});
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
            result = <Cardlet pageArgs={this.props.pageArgs} {...props} />;
            instances[key] = result;
        }

        return result;
    }

    render() {

        if (!this.state.data) {
            return '';
        }

        let self = this;
        let toCardlet = function (props) {
            return self.getCardletInstance(props);
        };

        let cardlets = this.state.data.cardlets;

        //TODO add ability to setup 'component' field for any cardlet
        let nodeHeader = cardlets.find(c => c.regionId == 'node-header');
        if (nodeHeader) {
            //nodeHeader.component = 'js/citeck/modules/cardlets/node-header';
        }

        let topRegions = cardlets.filter(c => c.regionColumn == 'top');
        let leftRegions = cardlets.filter(c => c.regionColumn == 'left');
        let rightRegions = cardlets.filter(c => c.regionColumn == 'right');
        let bottomRegions = cardlets.filter(c => c.regionColumn == 'bottom');

        let result = [];
        result.push(topRegions.map(toCardlet));
        result.push(
            <div className="yui-gc">
                <div className="yui-u first">
                    {leftRegions.map(toCardlet)}
                </div>
                <div className="yui-u">
                    {rightRegions.map(toCardlet)}
                </div>
            </div>
        );
        result.push(bottomRegions.map(toCardlet));

        return result;
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

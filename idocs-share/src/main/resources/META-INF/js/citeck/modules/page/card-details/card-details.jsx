
import 'js/citeck/lib/fetch';
import React from 'react';
import SurfRegion from '../../surf/surf-region';
import $ from 'jquery';

export class CardDetails extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            cardlets: []
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
            this.setState(json);
        });
    }

    render() {

        let self = this;
        let pageArgs = this.props.pageArgs;
        let toCardlet = function (props) {
            return <Cardlet cacheBust={self.cacheBust} pageArgs={pageArgs} {...props} />;
        };

        let cardlets = this.state.cardlets;

        //TODO add ability to setup 'component' field for any cardlet
        let nodeHeader = cardlets.find(c => c.regionId == 'node-header');
        if (nodeHeader) {
            //nodeHeader.component = 'js/citeck/modules/cardlets/node-header';
        }

        let topRegions = cardlets.filter(c => c.regionColumn == 'top');
        let leftRegions = cardlets.filter(c => c.regionColumn == 'left');
        let rightRegions = cardlets.filter(c => c.regionColumn == 'right');
        let bottomRegions = cardlets.filter(c => c.regionColumn == 'bottom');

        let createUploaderRegion = function (id) {
            return <SurfRegion args={{
                regionId: id,
                scope: 'template',
                templateId: "card-details",
                cacheAge: 1000,
                cb: self.props.cacheBust
            }}/>
        };

        return [
            <div id="alf-hd">
                <SurfRegion args={{
                    regionId: "share-header",
                    scope: "global",
                    chromeless: "true",
                    pageid: "card-details",
                    site: pageArgs.site,
                    theme: pageArgs.theme,
                    cacheAge: 300,
                    cb: this.props.cacheBust
                }} />
            </div>,
            <div id="bd">
                {topRegions.map(toCardlet)}
                <div className="yui-gc">
                    <div className="yui-u first">
                        {leftRegions.map(toCardlet)}
                    </div>
                    <div className="yui-u">
                        {rightRegions.map(toCardlet)}
                    </div>
                </div>
                {bottomRegions.map(toCardlet)}
                {[
                    createUploaderRegion('html-upload'),
                    createUploaderRegion('flash-upload'),
                    createUploaderRegion('dnd-upload'),
                    createUploaderRegion('archive-and-download'),
                    createUploaderRegion('file-upload')
                ]}
            </div>
        ];
    }
}

export class Cardlet extends React.Component {

    constructor(props) {
        super(props);
        if (props.component) {
            this.state = {
                component: 'Загрузка...'
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

                if (component.cssDependencies) {

                    let loadedDeps = Cardlet.prototype.loadedCssDeps;
                    if (!loadedDeps) {
                        loadedDeps = {};
                        Cardlet.prototype.loadedCssDeps = loadedDeps;
                    }

                    for (let dep of component.cssDependencies) {
                        if (!loadedDeps[dep]) {
                            $('<link>').attr({
                                type: 'text/css',
                                rel: 'stylesheet',
                                href: '/share/res/' + dep + '.css?' + self.props.cacheBust
                            }).appendTo('head');
                            loadedDeps[dep] = true;
                        }
                    }
                }
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

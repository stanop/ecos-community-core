
import React from 'react';
import {SurfRegion} from "../surf/surf-region";

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
        if (this.props.mode) {
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

        let pageArgs = this.props.pageArgs;
        let toCardlet = function (props) {
            return <Cardlet pageArgs={pageArgs} {...props} />;
        };

        let cardlets = this.state.cardlets;

        let topRegions = cardlets.filter(c => c.regionColumn == 'top');
        let leftRegions = cardlets.filter(c => c.regionColumn == 'left');
        let bottomRegions = cardlets.filter(c => c.regionColumn == 'bottom');
        let rightRegions = cardlets.filter(c => c.regionColumn == 'right');

        return <div>
            <div id="alf-hd">
                <SurfRegion args={{
                    regionId: "share-header",
                    scope: "global",
                    chromeless: "true",
                    pageid: "card-details",
                    site: pageArgs.site,
                    theme: pageArgs.theme,
                    cacheAge: 600
                }} />
            </div>
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
            </div>
            <div id="alf-ft">
                <SurfRegion className="sticky-footer" args={{
                    regionId: "footer",
                    scope: "global",
                    pageid: "card-details",
                    theme: pageArgs.theme,
                    cacheAge: 600
                }}/>
            </div>
        </div>;
    }
}

class Cardlet extends React.Component {

    constructor(props) {
        super(props);
        let regionProps = {
            regionId: props.regionId,
            htmlid: `cardlet-${Alfresco.util.generateDomId()}`,
            scope: 'page'
        };
        this.state = {
            regionArgs: Object.assign(regionProps, props.pageArgs)
        };
    }

    render() {
        return <div className='cardlet'
                    data-available-in-mobile={ this.props.availableInMobile }
                    data-position-index-in-mobile={ this.props.positionIndexInMobile }>
            <SurfRegion args={ this.state.regionArgs } />
        </div>;
    }
}

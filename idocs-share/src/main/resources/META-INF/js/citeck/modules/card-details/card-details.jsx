
import React from 'react';

export class CardDetails extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            cardlets: []
        };
    }

    componentDidMount() {
        let url = Alfresco.constants.PROXY_URI + "/citeck/card/cardlets?nodeRef=" + this.props.nodeRef;
        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            this.setState(json);
        });
    }

    render() {
        let counter = 0;

        let cardlets = this.state.cardlets.map(c => {
            return {
                data: c,
                component: <Cardlet htmlid={"some-cardlet-id-" + counter++} nodeRef = {this.props.nodeRef} {...c} />
            }
        });

        let topRegions = cardlets.filter(c => c.data.regionColumn == 'top');
        let leftRegions = cardlets.filter(c => c.data.regionColumn == 'left');
        let bottomRegions = cardlets.filter(c => c.data.regionColumn == 'bottom');
        let rightRegions = cardlets.filter(c => c.data.regionColumn == 'right');

        return <div id="bd">
            {/*<@region id="actions-common" scope="template" />*/}
                {topRegions.map(c => c.component)}
            <div className="yui-gc">
                <div className="yui-u first">
                    {leftRegions.map(c => c.component)}
            </div>
            <div className="yui-u">
                {rightRegions.map(c => c.component)}
            </div>
            </div>
            {bottomRegions.map(c => c.component)}
        </div>;
    }
}

class Cardlet extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            innerHtml: "Загрузка..."
        };
    }

    componentDidMount() {
        let requestArgs = [
            'nodeRef=' + this.props.nodeRef,
            'regionId=' + this.props.regionId,
            'htmlid=' + this.props.htmlid
        ];
        let url = "/share/service/citeck/card-details/cardlet?" + requestArgs.join('&');
        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.text();
        }).then(html => {
            this.setState({
                innerHtml: html
            });
        });
    }

    render() {
        return <div className='cardlet'
                    data-available-in-mobile={ this.props.availableInMobile }
                    data-position-index-in-mobile={ this.props.positionIndexInMobile }
                    dangerouslySetInnerHTML={{ __html: this.state.innerHtml }}
        />;
    }
}

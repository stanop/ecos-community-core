
import React from 'react';

export class CardDetails extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            cardlets: []
        };
    }

    componentDidMount() {
        let requestArgs = [
            'nodeRef=' + this.props.nodeRef
        ];
        if (this.props.mode) {
            requestArgs.push('mode=' + this.props.mode);
        }
        let url = Alfresco.constants.PROXY_URI + "/citeck/card/cardlets?" + requestArgs.join('&');
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
                component: <Cardlet index={counter++} pageArgs={this.props} {...c} />
            }
        });

        let topRegions = cardlets.filter(c => c.data.regionColumn == 'top');
        let leftRegions = cardlets.filter(c => c.data.regionColumn == 'left');
        let bottomRegions = cardlets.filter(c => c.data.regionColumn == 'bottom');
        let rightRegions = cardlets.filter(c => c.data.regionColumn == 'right');

        return <div id="bd">
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
        let htmlid = `cardlet-${props.regionId}-${props.regionColumn}-${props.regionPosition}-${props.index}`;
        this.state = {
            htmlid: htmlid,
            cardletRootId: htmlid + '-root'
        };
    }

    componentDidMount() {

        let requestArgs = [
            'regionId=' + this.props.regionId,
            'htmlid=' + this.state.htmlid
        ];

        for (let pageArg in this.props.pageArgs) {
            if (this.props.pageArgs.hasOwnProperty(pageArg)) {
                requestArgs.push(pageArg + '=' + this.props.pageArgs[pageArg]);
            }
        }

        let url = "/share/service/citeck/card-details/cardlet?" + requestArgs.join('&');

        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.text();
        }).then(text => {

            let scriptSrcRegexp = /<script type="text\/javascript" src="\/share\/res\/([^_]+)_\S+?.js"><\/script>/g;
            let dependencies = [];
            text = text.replace(scriptSrcRegexp, function (match, jsSrc) {
                dependencies.push(jsSrc);
                return '';
            });

            if (dependencies.length > 0) {

                let inlineScriptRegexp = /<script type="text\/javascript">\/\/<!\[CDATA\[([\S\s]+?)\/\/]]><\/script>/g;
                let inlineScripts = [];

                text = text.replace(inlineScriptRegexp, function (match, inlineScript) {
                    inlineScripts.push(inlineScript);
                    return '';
                });

                text += '\n' + '<script type="text/javascript">' +
                                   '//<![CDATA[\n' + inlineScripts.join('\n') + '\n\/\/]]>' +
                               '</script>';
            }

            this.resolveDependencies(dependencies, 0, () => {
                $('#' + this.state.cardletRootId).html(text);
            });
        });
    }

    resolveDependencies(dependencies, idx, callback) {
        if (idx >= dependencies.length) {
            callback();
        } else {
            require([dependencies[idx]], () => this.resolveDependencies(dependencies, idx + 1, callback));
        }
    }

    render() {
        return <div id={ this.state.cardletRootId } className='cardlet'
                    data-available-in-mobile={ this.props.availableInMobile }
                    data-position-index-in-mobile={ this.props.positionIndexInMobile }>
            Загрузка...
        </div>;
    }
}

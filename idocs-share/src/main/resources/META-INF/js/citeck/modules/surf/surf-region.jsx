import React from 'react';
import {utils as CiteckUtils} from 'js/citeck/modules/utils/citeck';
import 'lib/underscore'

export default class SurfRegion extends React.Component {

    constructor(props) {
        super(props);

        let htmlid = props.htmlid;
        if (!htmlid) {
            let idCounter = SurfRegion.prototype.idCounter || 0;
            htmlid = `SurfRegion-id-${idCounter}`;
            SurfRegion.prototype.idCounter = idCounter + 1;
        }

        let additionalArgs = {
            htmlid: htmlid
        };
        if (props.args.cacheAge && window.dojoConfig) {
            let cacheBust = window.dojoConfig.cacheBust;
            if (cacheBust) {
                additionalArgs['cb'] = cacheBust;
            }
        }

        this.state = {
            queryArgs: Object.assign(props.args, additionalArgs),
            rootId: `${htmlid}-root`
        }
    }

    componentDidMount() {
        let self = this;
        if (_.isUndefined(this.state.initialized)) {
            self.setState({
                loaded: false
            });
            CiteckUtils.loadHtml(
                '/share/service/citeck/surf/region',
                this.state.queryArgs,
                text => self.setState({
                    innerHtml: {__html: text},
                    loaded: true
                })
            );
        }
    }

    render() {
        return <div id={this.state.rootId} dangerouslySetInnerHTML={this.state.innerHtml}
                    className={this.props.className}>
        </div>;
    }
}
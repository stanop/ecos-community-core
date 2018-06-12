import React from 'react';
import $ from 'jquery';

export default class SurfRegion extends React.Component {

    constructor(props) {
        super(props);

        let htmlid = props.htmlid;
        if (!htmlid) {
            let idCounter = SurfRegion.prototype.idCounter || 0;
            htmlid = `SurfRegion-id-${idCounter}`;
            SurfRegion.prototype.idCounter = idCounter + 1;
        }
        this.state = {
            queryArgs: Object.assign(props.args, { htmlid: htmlid }),
            rootId: `${htmlid}-root`
        }
    }

    componentDidMount() {

        fetch("/share/service/citeck/surf/region?" + $.param(this.state.queryArgs), {
            credentials: 'include'
        }).then(response => {
            return response.text();
        }).then(text => {

            let scriptSrcRegexp = /<script type="text\/javascript" src="\/share\/res\/(\S+)_[^_]+?\.js"><\/script>/g;
            let dependencies = [];
            text = text.replace(scriptSrcRegexp, function (match, jsSrc) {
                if (jsSrc != "jquery/jquery") {
                    dependencies.push(jsSrc);
                }
                return '';
            });

            this.resolveDependencies(dependencies, 0, () => {
                $('#' + this.state.rootId).html(text);
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
        return <div id={this.state.rootId}
                    className={this.props.className}>
            Загрузка...
        </div>;
    }
}
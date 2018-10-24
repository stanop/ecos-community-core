import React from "react";
import NodeCardlet from '../node-cardlet';

import 'xstyle!./tasks-manual.css';

export default class TasksManual extends NodeCardlet {

    static getFetchUrl(ownProps) {
        return '/share/proxy/alfresco/citeck/document/tasks-manual?nodeRef=' + ownProps.nodeRef;
    }

    render() {

        let props = this.props;
        let data = this.props.data;

        let isLoading = props.isFetching || data.nodePendingUpdate;

        let loadingClass = isLoading !== false ? 'loading' : '';

        return <div id="cardlet-tasks-desc" className={loadingClass}>
            {data.tasks.map(t => {
                return <div id={`tasks-desc-${t.id}`}>
                    <div><span>Задача: ${t.title}</span></div>
                    <div>
                        <span>Описание:</span>
                        <div dangerouslySetInnerHTML={{__html: t.description}} />
                    </div>
                </div>
            })}
        </div>;
    }
}

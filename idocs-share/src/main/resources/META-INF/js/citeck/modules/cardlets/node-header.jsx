import "js/citeck/lib/fetch";
import React from "react";

export default class NodeHeader extends React.Component {

    get cssDependencies() {
        return [
            'components/node-details/node-header'
        ];
    }

    constructor(props) {
        super(props);
        this.state = {
            loading: true
        };
    }

    componentDidMount() {
        let nodeRef = this.props.pageArgs.nodeRef;
        let url = "/share/proxy/alfresco/citeck/components/node-header?nodeRef=" + nodeRef;
        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            this.setState({
                loading: false,
                data: json
            });
        });
    }

    getModifiedInfo() {

        let data = this.state.data;
        let modifier = data.modifier;

        let userName = modifier.userName;

        let displayName = `${modifier.firstName || ""} ${modifier.lastName || ""}`;
        if (displayName == ' ') {
            displayName = userName;
        }

        let modifierLink = Alfresco.util.userProfileLink(userName, displayName, 'class="theme-color-1"');

        let dateFormat = Alfresco.util.message("date-format.default");
        let modifiedDate = Alfresco.util.fromISO8601(data.modified);
        let modified = Alfresco.util.formatDate(modifiedDate, dateFormat);

        let message = Alfresco.util.message('label.modified-by-user-on-date');

        return YAHOO.lang.substitute(message, [modifierLink, modified]);
    }

    render() {

        if (this.state.loading) {
            return <span>Загрузка...</span>;
        }

        let data = this.state.data;

        return <div>
            <div className="node-header">
                <div className="node-info">
                    <img src={`/share/res/components/images/filetypes/${data.fileExtension}-file-48.png`}
                         onError="this.src='/share/res/components/images/filetypes/generic-file-48.png'"
                         title={data.displayName} className="node-thumbnail" width="48"/>
                    <h1 className="thin dark">
                        {data.displayName}<span id="document-version" className="document-version">{data.version}</span>
                    </h1>
                    <div>
                        <span className="item-modifier" dangerouslySetInnerHTML={{__html: this.getModifiedInfo()}} />
                        <span className="item item-separator item-social">
                            <a href="#" className="favourite-action theme-color-1 favourite-document"
                               title="Добавить документ в Избранное">Избранное</a>
                        </span>
                        <span className="item item-separator item-social">
                            <a href="#" className="like-action theme-color-1"
                               title="Мне нравится этот документ">Мне нравится</a>
                        <span className="likes-count">0</span></span>
                        <span className="item item-separator item-social">
                            <a href="#" name="@commentNode"
                               rel="workspace://SpacesStore/43150330-71cd-4f0c-99de-23c5568e3258"
                               className="theme-color-1 comment hasComments page_x002e_node-header_x002e_card-details_x0023_default"
                               title="Прокомментировать этот документ">Комментировать</a>
                            <span className="comment-count">0</span>
                        </span>
                        <span className="item item-separator item-social">
                            <a href="#" className="quickshare-action" title="Опубликовать документ">Опубликовать</a>
                            <span className="quickshare-indicator">&nbsp;</span>
                        </span>
                    </div>
                </div>
            </div>
        </div>;
    }
}

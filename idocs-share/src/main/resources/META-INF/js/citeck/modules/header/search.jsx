import React from 'react';
import "xstyle!js/citeck/modules/header/search.css";

export default class Search extends React.Component {

    constructor(props) {
        super();
        this.state = {
            currentValue: ""
        };
    }

    isEnter(event) {
        if (event.keyCode === 13) {
            let url = this.props.searchPageUrl || "dp/ws/faceted-search#searchTerm=" + this.generateSearchTerm(this.state.currentValue) + "&scope=repo";

            window.location = Alfresco.constants.URL_PAGECONTEXT + url;
        }
    }

    generateSearchTerm(terms) {
        let searchTerm = this.props.hiddenSearchTerms ? "(" + terms + ") " + this.props.hiddenSearchTerms : terms;

        return encodeURIComponent(searchTerm);
    }

    addText(event) {
        this.setState({currentValue: event.target.value});
    }

    onSearchClearClick(event) {
        this.setState({currentValue: ""});
    }

    render() {
        const {className, placeholder} = this.props;
        let cls = className ? className : "alfresco-header-SearchBox";
        let searchPlaceholder = placeholder ? Alfresco.util.message(placeholder) : Alfresco.util.message("Search files, people, sites");
        return <div className={cls} id="HEADER_SEARCH_BOX" >
                   <div className="alfresco-header-SearchBox-inner">
                       <input type="text"
                              id="HEADER_SEARCHBOX_FORM_FIELD"
                              className="alfresco-header-SearchBox-text"
                              placeholder={searchPlaceholder}
                              onChange={this.addText.bind(this)}
                              value={this.state.currentValue}
                              onKeyDown={this.isEnter.bind(this)} />
                       <div className="alfresco-header-SearchBox-clear">
                            <a href="#"
                               title="Clear"
                               onClick={this.onSearchClearClick.bind(this)}>
                                <div>&nbsp;</div>
                            </a>
                        </div>
                   </div>
            </div>;
    }
}
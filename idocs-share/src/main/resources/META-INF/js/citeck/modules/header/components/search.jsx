import React from 'react';
import SearchDropdown from 'js/citeck/modules/header/components/search-dropdown';
import "xstyle!js/citeck/modules/header/components/search.css";

export default class Search extends React.Component {
    state = {
        searchText: ""
    };

    onKeyDown = (e) => {
        if (e.key === 'Enter') {
            let url = this.props.searchPageUrl || "dp/ws/faceted-search#searchTerm=" + this.generateSearchTerm(this.state.searchText) + "&scope=repo";
            window.location = Alfresco.constants.URL_PAGECONTEXT + url;
        }
    };

    generateSearchTerm = (terms) => {
        let searchTerm = this.props.hiddenSearchTerms ? "(" + terms + ") " + this.props.hiddenSearchTerms : terms;

        return encodeURIComponent(searchTerm);
    };

    onTextChange = (e) => {
        this.setState({searchText: e.target.value});
    };

    onSearchClearClick = () => {
        this.setState({searchText: ""});
    };

    render() {
        const { placeholder } = this.props;
        const searchPlaceholder = placeholder ? Alfresco.util.message(placeholder) : Alfresco.util.message("Search files, people, sites");

        return (
            <div className="alfresco-header-SearchBox share-header-search" id="HEADER_SEARCH_BOX" >
                <div className="alfresco-header-SearchBox-inner share-header-search__inner">
                    <SearchDropdown />
                    <input
                        id="HEADER_SEARCHBOX_FORM_FIELD"
                        className="alfresco-header-SearchBox-text"
                        type="text"
                        placeholder={searchPlaceholder}
                        onChange={this.onTextChange}
                        value={this.state.searchText}
                        onKeyDown={this.onKeyDown}
                    />
                    <div
                        className="share-header-search__clear-button"
                        title={Alfresco.util.message("Clear")} // TODO set correct message
                        onClick={this.onSearchClearClick}
                    >
                        <i className={"fa fa-times-circle"} />
                    </div>
                </div>
            </div>
        );
    }
}
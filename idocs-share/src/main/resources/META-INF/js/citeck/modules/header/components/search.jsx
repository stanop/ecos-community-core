import React from 'react';
import { connect } from "react-redux";
import SearchDropdown from './search-dropdown';
import SearchAutocomplete from './search-autocomplete';
import ClickOutside from "./click-outside";
import { toggleAutocompleteVisibility, fetchAutocomplete } from "../actions";
import { t, generateSearchTerm } from '../misc/util';

const _ = window._;

const mapStateToProps = state => ({
    autocompleteIsVisible: state.searchAutocomplete.isVisible
});

const mapDispatchToProps = dispatch => ({
    fetchAutocomplete: payload => dispatch(fetchAutocomplete(payload)),
    showAutocomplete: payload => dispatch(toggleAutocompleteVisibility(true)),
    hideAutocomplete: payload => dispatch(toggleAutocompleteVisibility(false))
});

class Search extends React.Component {
    searchPlaceholder = t('search.label');
    clearButtonTitle = t('search.clear');

    state = {
        searchText: ''
    };

    onKeyDown = e => {
        const { searchPageUrl, hiddenSearchTerms } = this.props;
        const { searchText } = this.state;
        if (e.key === 'Enter') {
            let url = searchPageUrl || "dp/ws/faceted-search#searchTerm=" + generateSearchTerm(searchText, hiddenSearchTerms) + "&scope=repo";
            window.location = window.Alfresco.constants.URL_PAGECONTEXT + url;
        }
    };

    onSearchClearClick = () => {
        this.setState({ searchText: "" });
    };

    onTextChange = e => {
        this.setState({ searchText: e.target.value }, this.fetchAutocomplete);
    };

    fetchAutocomplete = _.debounce(() => {
        this.props.fetchAutocomplete(this.state.searchText);
    }, 500);

    render() {
        const { showAutocomplete, hideAutocomplete, autocompleteIsVisible } = this.props;
        const { searchText } = this.state;
        return (
            <div id="HEADER_SEARCH_BOX" className="alfresco-header-SearchBox share-header-search">
                <div className="alfresco-header-SearchBox-inner share-header-search__inner">
                    <SearchDropdown />
                    <ClickOutside
                        handleClickOutside={autocompleteIsVisible && hideAutocomplete}
                        className='share-header-search__click_outside_wrapper'
                    >
                        <input
                            id="HEADER_SEARCHBOX_FORM_FIELD"
                            className="alfresco-header-SearchBox-text"
                            type="text"
                            placeholder={this.searchPlaceholder}
                            onChange={this.onTextChange}
                            value={searchText}
                            onKeyDown={this.onKeyDown}
                            onFocus={showAutocomplete}
                        />
                        <SearchAutocomplete />
                    </ClickOutside>
                    <div
                        className="share-header-search__clear-button"
                        title={this.clearButtonTitle}
                        onClick={this.onSearchClearClick}
                    >
                        <i className={"fa fa-times-circle"} />
                    </div>
                </div>
            </div>
        );
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Search);
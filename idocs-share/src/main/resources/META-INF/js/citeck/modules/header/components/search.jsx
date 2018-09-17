import React from 'react';
import { compose, withProps, withState, withHandlers } from 'recompose';
import SearchDropdown from './search-dropdown';
import { t } from '../misc/util';
import "xstyle!js/citeck/modules/header/components/search.css";

const Search = ({ searchText, onTextChange, onKeyDown, onSearchClearClick, searchPlaceholder, clearButtonTitle }) => (
    <div id="HEADER_SEARCH_BOX" className="alfresco-header-SearchBox share-header-search">
        <div className="alfresco-header-SearchBox-inner share-header-search__inner">
            <SearchDropdown />
            <input
                id="HEADER_SEARCHBOX_FORM_FIELD"
                className="alfresco-header-SearchBox-text"
                type="text"
                placeholder={searchPlaceholder}
                onChange={onTextChange}
                value={searchText}
                onKeyDown={onKeyDown}
            />
            <div
                className="share-header-search__clear-button"
                title={clearButtonTitle}
                onClick={onSearchClearClick}
            >
                <i className={"fa fa-times-circle"} />
            </div>
        </div>
    </div>
);

const generateSearchTerm = (terms, hiddenSearchTerms) => {
    let searchTerm = hiddenSearchTerms ? "(" + terms + ") " + hiddenSearchTerms : terms;

    return encodeURIComponent(searchTerm);
};

const enhance = compose(
    withProps(props => {
        return {
            searchPlaceholder: props.placeholder ? t(props.placeholder) : t("Search files, people, sites"),
            clearButtonTitle: t("Clear") // TODO set correct message
        }
    }),
    withState('searchText', 'updateSearchText', ''),
    withHandlers({
        onKeyDown: props => e => {
            const { searchPageUrl, searchText, hiddenSearchTerms } = props;
            if (e.key === 'Enter') {
                let url = searchPageUrl || "dp/ws/faceted-search#searchTerm=" + generateSearchTerm(searchText, hiddenSearchTerms) + "&scope=repo";
                window.location = window.Alfresco.constants.URL_PAGECONTEXT + url;
            }
        },
        onSearchClearClick: props => () => {
            props.updateSearchText("");
        },
        onTextChange: props => e => {
            props.updateSearchText(e.target.value);
        }
    })
);

export default enhance(Search);
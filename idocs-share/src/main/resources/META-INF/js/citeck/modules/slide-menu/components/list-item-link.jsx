import React from 'react';
import { connect } from "react-redux";
import { setSelectedId, toggleExpanded } from "../actions";
import { t } from "../../common/util";
import ListItemIcon from "./list-item-icon";

const SELECTED_MENU_ITEM_ID_KEY = 'selectedMenuItemId';
const PAGE_PREFIX = '/share/page';

const mapStateToProps = (state, ownProps) => ({
    selectedId: state.leftMenu.selectedId
});

const mapDispatchToProps = (dispatch, ownProps) => ({
    onSelectItem: id => {
        sessionStorage && sessionStorage.setItem && sessionStorage.setItem(SELECTED_MENU_ITEM_ID_KEY, id);
        ownProps.toggleSlideMenu();
        dispatch(setSelectedId(id));
    },
    setExpanded: id => dispatch(toggleExpanded(id))
});

const ListItemLink = ({item, onSelectItem, selectedId, nestedList, setExpanded, isNestedListExpanded, parentParams}) => {
    let itemId = item.id;
    let label = t(item.label);

    let classes = ['slide-menu-list__link'];
    if (selectedId === itemId) {
        classes.push('slide-menu-list__link_selected');
    }

    let targetUrl = null;
    let clickHandler = null;
    if (item.action) {
        switch (item.action.type) {
            case 'FILTER_LINK':
                // hack for journal filters
                if (parentParams && parentParams.type === 'JOURNAL_LINK') {
                    // /share/page/site/orders/journals2/list/main#journal=workspace://SpacesStore/journal-orders-internal&filter=workspace://SpacesStore/journal-internal-me-init
                    if (parentParams && parentParams.parent && parentParams.parent.type === 'SITE_LINK') {
                        targetUrl = `${PAGE_PREFIX}/site/${parentParams.parent.params.siteName}/journals2/list/main#journal=${parentParams.params.journalRef}&filter=${item.action.params.filterRef}`;
                    } else {
                        targetUrl = `${PAGE_PREFIX}/journals2/list/tasks#journal=${parentParams.params.journalRef}&filter=${item.action.params.filterRef}`;
                    }
                }
                break;
            case 'JOURNAL_LINK':
                // hack for site journals
                if (parentParams && parentParams.type === 'SITE_LINK') {
                    targetUrl = `${PAGE_PREFIX}/site/${parentParams.params.siteName}/journals2/list/main#journal=${item.action.params.journalRef}&filter=`;
                } else {
                    targetUrl = `${PAGE_PREFIX}/journals2/list/tasks#journal=${item.action.params.journalRef}&filter=`;
                }
                break;
            case 'PAGE_LINK':
                let sectionPostfix = item.action.params.section ? item.action.params.section : '';
                targetUrl = `${PAGE_PREFIX}/${item.action.params.pageId}${sectionPostfix}`;
                break;
            case 'SITE_LINK':
                targetUrl = `${PAGE_PREFIX}?site=${item.action.params.siteName}`;
                break;
        }
    }

    return (
        <a
            href={targetUrl}
            onClick={() => {
                onSelectItem(itemId);
                console.log('item, parentParams', item, parentParams);
                // TODO
                typeof clickHandler === 'function' && clickHandler();
            }}
            className={classes.join(' ')}
        >
            <ListItemIcon item={item} />
            <span className={'slide-menu-list__link-label'}>{label}</span>
        </a>
    );
};

export default connect(mapStateToProps, mapDispatchToProps)(ListItemLink);


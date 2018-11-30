import React from 'react';
import { connect } from "react-redux";
import ListItemLink from "./list-item-link";
import ListItemCreateSite from "./list-item-create-site";
import { toggleExpanded } from "../actions";

const mapDispatchToProps = (dispatch, ownProps) => ({
    setExpanded: id => dispatch(toggleExpanded(id))
});

const ListItem = (props) => {
    let {item, nestedList, setExpanded, isNestedListExpanded} = props;
    let itemId = item.id;

    let toggleCollapse = null;
    if (nestedList) {
        let classes = ['slide-menu-list__toggle-collapse'];
        if (isNestedListExpanded) {
            classes.push('slide-menu-list__toggle-collapse_expanded');
        }

        toggleCollapse = <div className={classes.join(' ')} onClick={() => setExpanded(itemId)} />;
    }

    let component = null;
    if (item.action) {
        switch (item.action.type) {
            case 'CREATE_SITE':
                component = <ListItemCreateSite {...props} />;
                break;
            case 'FILTER_LINK':
            case 'JOURNAL_LINK':
            case 'PAGE_LINK':
            case 'SITE_LINK':
            default:
                component = <ListItemLink {...props} withNestedList={nestedList} />;
                break;
        }
    }

    return (
        <li id={itemId} className='slide-menu-list__item'>
            {toggleCollapse}
            {component}
            {nestedList}
        </li>
    );
};

export default connect(null, mapDispatchToProps)(ListItem);

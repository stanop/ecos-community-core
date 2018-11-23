import React from 'react';
import { connect } from "react-redux";
import ListItem from './list-item';

const mapStateToProps = (state) => ({
    expandableItems: state.leftMenu.expandableItems
});

const ListPure = ({items, toggleSlideMenu, isExpanded, isNested, expandableItems, parentParams}) => {
    const listContent = items.map(item => {
        let nestedList = null;
        let isNestedListExpanded = false;
        if (item.items && item.items.length > 0) {
            if (expandableItems && expandableItems.length > 0) {
                const expandableItem = expandableItems.find(fi => fi.id === item.id);
                isNestedListExpanded = expandableItem.isNestedListExpanded;
            }

            let pp = item.action;
            if (parentParams) {
                pp.parent = parentParams;
            }

            nestedList = (
                <List
                    items={item.items}
                    parentParams={pp}
                    toggleSlideMenu={toggleSlideMenu}
                    isNested={true}
                    isExpanded={isNestedListExpanded}
                />
            );
        }

        return (
            <ListItem
                key={item.id}
                toggleSlideMenu={toggleSlideMenu}
                item={item}
                nestedList={nestedList}
                isNestedListExpanded={isNestedListExpanded}
                parentParams={parentParams}
            />
        );
    });

    let classes = ['slide-menu-collapsible-list'];

    if (isExpanded) {
        classes.push('slide-menu-collapsible-list_expanded');
    }

    if (isNested) {
        classes.push('slide-menu-collapsible-list_nested');
    }

    return (
        <ul className={classes.join(' ')}>
            {listContent}
        </ul>
    );
};

const List = connect(mapStateToProps)(ListPure);
export default List;

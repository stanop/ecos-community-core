import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';

const ListItem = ({item, toggleSlideMenu, isSelected, nestedList, toggleCollapse}) => {
    if (item.sectionTitle) {
        return (
            <li id={item.id} className='slide-menu-list__item list-divider'>
                <span className='list-divider__text'>{Alfresco.util.message(item.sectionTitle)}</span>
                {nestedList}
            </li>
        );
    }

    let label = Alfresco.util.message(item.label || `header.${item.id}.label`);
    let icon = <i className={`fa fa-menu-default-icon ${item.id}`} />;
    let targetUrl = null;
    if (item.url) {
        targetUrl = item.url;
    }

    // TODO handleControl
    let clickHandler = null;
    // if (item.control && item.control.type) {
    //     clickHandler = event => {
    //         event.preventDefault();
    //         handleControl(control.type, control.payload, dispatch);
    //     };
    // }
    if (item.clickEvent) {
        clickHandler = item.clickEvent;
    }

    let classes = ['slide-menu-list__link'];
    if (isSelected) {
        classes.push('slide-menu-list__link_selected');
    }

    return (
        <li id={item.id} className='slide-menu-list__item'>
            {toggleCollapse}
            <a
                href={targetUrl}
                onClick={() => {
                    // setSelected(itemId);
                    toggleSlideMenu();
                    eval(clickHandler);
                }}
                className={classes.join(' ')}
            >
                {icon}
                <span className={'slide-menu-list__link-label'}>{label}</span>
            </a>
            {nestedList}
        </li>
    );
};

export default ListItem;


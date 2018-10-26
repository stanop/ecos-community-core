import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';

export const LogoBlock = ({smallLogo, largeLogo}) => {
    const logoLinkHref = '/share/page';

    return (
        <div className='slide-menu-logo'>
            <img className='slide-menu-logo__small' src={smallLogo} />
            <div className='slide-menu-logo__large'>
                <a href={logoLinkHref}>
                    <img src={largeLogo} />
                </a>
            </div>
        </div>
    );
};

export const ListBlock = ({items, toggleSlideMenu}) => {
    const scrollBarStyle = { height: 'calc(100% - 40px)' };
    const verticalTrack = props => <div {...props} className="slide-menu-list__vertical-track"/>;
    return (
        <Scrollbars
            className='slide-menu-list'
            autoHide
            style={scrollBarStyle}
            renderTrackVertical={verticalTrack}
        >
            <nav>
                <List items={items} toggleSlideMenu={toggleSlideMenu} />
            </nav>
        </Scrollbars>
    );
};

const List = ({items, toggleSlideMenu}) => {
    const listContent = items.map((item, idx) => {

        let nestedList = null;
        if (item.widgets && item.widgets.length) {
            nestedList = <List items={item.widgets} toggleSlideMenu={toggleSlideMenu} />;
        }

        if (item.sectionTitle) {
            return (
                <li id={item.id}>
                    {Alfresco.util.message(item.sectionTitle)}
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

        let clickHandler = null;
        // if (item.control && item.control.type) {
        //     clickHandler = event => {
        //         event.preventDefault();
        //         handleControl(control.type, control.payload, dispatch);
        //     };
        // }

        // TODO handleControl
        if (item.clickEvent) {
            clickHandler = item.clickEvent;
        }

        return (
            <li id={item.id}>
                <a
                    name={item.id}
                    href={targetUrl}
                    onClick={() => {
                        toggleSlideMenu();
                        eval(clickHandler);
                    }}
                >
                    {icon}
                    {label}
                </a>
                {nestedList}
            </li>
        );
    });

    return (
        <ul>{listContent}</ul>
    );
};
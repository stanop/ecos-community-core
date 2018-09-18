import React from 'react';
import CreateCaseWidget from './components/create-case-widget';
import UserMenu from './components/user-menu';
import SitesMenu from './components/sites-menu';
import Search from './components/search';
import { siteMenuItems, getUserMenuItems } from './misc/fake-data';
import "xstyle!js/citeck/modules/header/share-header.css";

const ShareHeader = ({ userName }) => {
    const userMenuItems = getUserMenuItems(userName);

    return (
        <div id='SHARE_HEADER' className='alfresco-header-Header'>
            <div className="alfresco-layout-LeftAndRight__left">
                <CreateCaseWidget />
            </div>
            <div className="alfresco-layout-LeftAndRight__right">
                <UserMenu
                    items={userMenuItems}
                />
                <SitesMenu />
                <Search />
            </div>
        </div>
    );
};

export default ShareHeader;
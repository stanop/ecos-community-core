import React from 'react';
import CreateCaseWidget from './components/create-case-widget';
import UserMenu from './components/user-menu';
import SitesMenu from './components/sites-menu';
import Search from './components/search';
import { siteMenuItems, getUserMenuItems, createCaseMenuItems } from './misc/fake-data';
import "xstyle!js/citeck/modules/header/share-header.css";

const ShareHeader = ({ userName, userFullname, userNodeRef }) => {
    const userMenuItems = getUserMenuItems(userName);

    return (
        <div id='SHARE_HEADER' className='alfresco-header-Header'>
            <div className="alfresco-layout-LeftAndRight__left">
                <CreateCaseWidget items={createCaseMenuItems} />
            </div>
            <div className="alfresco-layout-LeftAndRight__right">
                <UserMenu
                    userName={userFullname}
                    userNodeRef={userNodeRef}
                    items={userMenuItems}
                />
                <SitesMenu items={siteMenuItems} />
                <Search />
            </div>
        </div>
    );
};

export default ShareHeader;
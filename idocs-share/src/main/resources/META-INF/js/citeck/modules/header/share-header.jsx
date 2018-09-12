import React from 'react';
import UserMenu from 'js/citeck/modules/header/user-menu';
import SitesMenu from 'js/citeck/modules/header/sites-menu';
import Search from 'js/citeck/modules/header/search';
import { siteMenuItems, getUserMenuItems } from 'js/citeck/modules/header/misc/fake-data';

import "xstyle!js/citeck/modules/header/share-header.css";

const ShareHeader = ({ userName, userFullname, userNodeRef }) => {
    const userMenuItems = getUserMenuItems(userName);

    return (
        <div id='SHARE_HEADER'>
            <div className="alfresco-layout-LeftAndRight__left">

            </div>
            <div className="alfresco-layout-LeftAndRight__right">
                <UserMenu
                    userName={userFullname}
                    userNodeRef={userNodeRef}
                    items={userMenuItems}/>
                <SitesMenu
                    items={siteMenuItems} />
                <Search />
            </div>
        </div>
    );
};

export default ShareHeader;
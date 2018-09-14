import React from 'react';
import CreateCaseWidget from 'js/citeck/modules/header/components/create-case-widget';
import UserMenu from 'js/citeck/modules/header/components/user-menu';
import SitesMenu from 'js/citeck/modules/header/components/sites-menu';
import Search from 'js/citeck/modules/header/components/search';
import { siteMenuItems, getUserMenuItems, createCaseMenuItems } from 'js/citeck/modules/header/misc/fake-data';

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
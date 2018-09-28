import React from 'react';
import CreateCaseWidget from './components/create-case-widget';
import UserMenu from './components/user-menu';
import SitesMenu from './components/sites-menu';
import Search from './components/search';
import "xstyle!js/citeck/modules/header/share-header.css";

import CustomModal from './components/custom-modal';

const ShareHeader = () => {
    return (
        <div id='SHARE_HEADER' className='alfresco-header-Header'>
            <div className="alfresco-layout-LeftAndRight__left">
                <CreateCaseWidget />
            </div>
            <div className="alfresco-layout-LeftAndRight__right">
                <UserMenu />
                <SitesMenu />
                <Search />
            </div>
            <CustomModal />
        </div>
    );
};

export default ShareHeader;
import React from 'react';
import { connect } from 'react-redux';
import CreateCaseWidget from './components/create-case-widget';
import UserMenu from './components/user-menu';
import SitesMenu from './components/sites-menu';
import Search from './components/search';
import "xstyle!js/citeck/modules/header/share-header.css";

import CustomModal from './components/custom-modal';

const mapStateToProps = state => ({
    isMobile: state.view.isMobile
});

const ShareHeader = ({ isMobile }) => {
    const hamburgerIcon = isMobile ? <label className='hamburger-icon' htmlFor="slide-menu-checkbox" /> : null;
    return (
        <div id='SHARE_HEADER' className='alfresco-header-Header'>
            <div className="alfresco-layout-LeftAndRight__left">
                <div id="HEADER_APP_MENU_BAR">
                    {hamburgerIcon}
                </div>
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

export default connect(mapStateToProps)(ShareHeader);
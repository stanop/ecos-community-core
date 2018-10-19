import React from 'react';
import { connect } from 'react-redux';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';

const SitesMenu = ({ items }) => {
    if (!Array.isArray(items) || items.length < 1) {
        return null;
    }

    const menuListItems = items.map((item, key) => <DropDownMenuItem key={key} data={item} />);

    return (
        <div id='HEADER_SITE_MENU'>
            <Dropdown className='custom-dropdown-menu' pullRight>
                <CustomToggle bsRole='toggle' className='site-dropdown-menu__toggle custom-dropdown-menu__toggle'>
                    <i className={'fa fa-cog'} />
                </CustomToggle>
                <Dropdown.Menu bsRole='menu' className='custom-dropdown-menu__body' id='HEADER_SITE_MENU__DROPDOWN'>
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    );
};

const mapStateToProps = (state) => ({
    items: state.siteMenu.items
});

export default connect(mapStateToProps)(SitesMenu);
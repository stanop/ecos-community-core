import React from 'react';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/components/dropdown-menu-item';
import CustomToggle from 'js/citeck/modules/header/components/dropdown-menu-custom-toggle';

const SitesMenu = ({ items, headerTitle, headerIcon }) => {
    let icon = headerIcon || "fa-cog";
    const menuListItems = items && items.length && items.map((item, key) => (
        <DropDownMenuItem
            key={key}
            targetUrl={item.targetUrl}
            image={item.image}
            icon={item.icon}
            label={item.label}
        />
    ));

    return (
        <div id="HEADER_SITE_MENU">
            <Dropdown className="custom-dropdown-menu" pullLeft>
                <CustomToggle bsRole="toggle" className="site-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <i className={"fa " + icon} />
                    {headerTitle}
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    );
};

export default SitesMenu;
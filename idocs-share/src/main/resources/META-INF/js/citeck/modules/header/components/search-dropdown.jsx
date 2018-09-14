import React, { Fragment } from 'react';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/components/dropdown-menu-item';
import CustomToggle from 'js/citeck/modules/header/components/dropdown-menu-custom-toggle';

const SearchDropdown = () => {
    const serachDropdownListItems = [
        <DropDownMenuItem
            key={0}
            targetUrl="/share/page/site/hr/advsearch"
            label="Расширенный поиск..." // TODO get from messages
        />
    ];

    return (
        <Fragment>
            <Dropdown className="search-dropdown-menu" pullLeft>
                <CustomToggle bsRole="toggle" className="search-dropdown-menu__toggle">
                    <i className={"fa fa-chevron-circle-down"} />
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu__body">
                    {serachDropdownListItems}
                </Dropdown.Menu>
            </Dropdown>
        </Fragment>
    );
};

export default SearchDropdown;
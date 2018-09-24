import React, { Fragment } from 'react';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';

const SearchDropdown = () => {
    const serachDropdownListItems = [
        <DropDownMenuItem
            key={0}
            data={{
                id: 'HEADER_SEARCH_BOX_ADVANCED_SEARCH',
                label: "header.advanced-search.label",
                targetUrl: "/share/page/site/hr/advsearch"
            }}
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
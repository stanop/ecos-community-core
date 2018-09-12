import React, { Fragment } from 'react';
import { Dropdown } from 'react-bootstrap';
import CustomToggle from 'js/citeck/modules/header/components/dropdown-menu-custom-toggle';

const SearchDropdown = () => {
    return (
        <Fragment>
            <Dropdown className="search-dropdown-menu" pullLeft>
                <CustomToggle bsRole="toggle" className="search-dropdown-menu-toggle">
                    <i className={"fa fa-chevron-circle-down"} />
                </CustomToggle>
                <Dropdown.Menu>
                    <p>TODO</p>
                </Dropdown.Menu>
            </Dropdown>
        </Fragment>
    );
};

export default SearchDropdown;
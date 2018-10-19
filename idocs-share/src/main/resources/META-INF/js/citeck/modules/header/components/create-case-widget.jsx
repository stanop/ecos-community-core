import React from 'react';
import { connect } from 'react-redux';
import { Dropdown } from 'react-bootstrap';
import CustomToggle from './dropdown-menu-custom-toggle';
import DropDownMenuGroup from './dropdown-menu-group';

const CreateCaseWidget = ({ items }) => {
    let menuListItems = null;
    if (Array.isArray(items) && items.length > 0) {
        menuListItems = items.map((item, key) => {
            return (
                <DropDownMenuGroup
                    key={key}
                    label={item.label}
                    items={item.items}
                    id={item.id}
                />
            );
        });
    }

    return (
        <div id='HEADER_CREATE_CASE'>
            <Dropdown className='custom-dropdown-menu' pullLeft>
                <CustomToggle bsRole='toggle' className='create-case-dropdown-menu__toggle custom-dropdown-menu__toggle'>
                    <i className={'fa fa-plus'} />
                </CustomToggle>
                <Dropdown.Menu bsRole='menu' className='custom-dropdown-menu__body' id='HEADER_CREATE_CASE__DROPDOWN'>
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const mapStateToProps = (state) => ({
    items: state.caseMenu.items
});

export default connect(mapStateToProps)(CreateCaseWidget);

import React from 'react';
import { connect } from 'react-redux';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';

const UserMenu = ({ userFullName, userPhotoUrl, items }) => {
    const userImage = userPhotoUrl ? (
        (
            <div className="user-photo-header">
                <div style={{backgroundImage: 'url(' + userPhotoUrl + ')'}} />
            </div>
        )
    ) : null;

    const menuListItems = items && items.length && items.map((item, key) => (
        <DropDownMenuItem
            key={key}
            data={item}
        />
    ));

    return (
        <div id='HEADER_USER_MENU'>
            <Dropdown id="HEADER_USER_MENU__DROPDOWN" className="custom-dropdown-menu" pullRight>
                <CustomToggle bsRole="toggle" className="user-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <span className="user-menu-username">{userFullName}</span>
                    {userImage}
                </CustomToggle>
                <Dropdown.Menu bsRole="menu" className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const mapStateToProps = (state) => ({
    userPhotoUrl: state.user.photo,
    userFullName: state.user.fullName,
    items: state.userMenu.items
});

export default connect(mapStateToProps)(UserMenu);
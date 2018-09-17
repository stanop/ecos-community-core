import React from 'react';
import { connect } from 'react-redux';
import { compose, lifecycle } from 'recompose';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';
import { loadUserMenuPhoto } from '../actions';

const UserMenu = ({ userName, userNodeRef, userPhotoUrl, items }) => {
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
            targetUrl={item.targetUrl}
            image={item.image}
            icon={item.icon}
            label={item.label}
            target={item.target}
        />
    ));

    return (
        <div id='HEADER_USER_MENU'>
            <Dropdown className="custom-dropdown-menu" pullRight>
                <CustomToggle bsRole="toggle" className="user-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <span className="user-menu-username">{userName}</span>
                    {userImage}
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const enhance = compose(
    lifecycle({
        componentDidMount() {
            const { userNodeRef, dispatch } = this.props;
            dispatch(loadUserMenuPhoto(userNodeRef));
        }
    }),
);

const mapStateToProps = (state, ownProps) => ({
    userPhotoUrl: state.userMenu.userPhoto,
});

export default connect(mapStateToProps)(enhance(UserMenu));
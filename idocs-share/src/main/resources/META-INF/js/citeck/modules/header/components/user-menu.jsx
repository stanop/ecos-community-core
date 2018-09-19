import React from 'react';
import { connect } from 'react-redux';
import { compose, lifecycle, withState } from 'recompose';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';
import { loadUserMenuPhoto } from '../actions';
import { makeUserMenuItems } from '../misc/util';

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
            <Dropdown className="custom-dropdown-menu" pullRight>
                <CustomToggle bsRole="toggle" className="user-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <span className="user-menu-username">{userFullName}</span>
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
    withState("items", "setItems", []),
    lifecycle({
        componentDidMount() {
            const { userNodeRef, userName, userIsAvailable, dispatch, setItems } = this.props;
            dispatch(loadUserMenuPhoto(userNodeRef));

            const userMenuItems = makeUserMenuItems(userName, userIsAvailable);
            setItems(userMenuItems);
        }
    }),
);

const mapStateToProps = (state, ownProps) => ({
    userPhotoUrl: state.user.photo,
    userName: state.user.name,
    userFullName: state.user.fullName,
    userNodeRef: state.user.nodeRef,
    userIsAvailable: state.user.isAvailable,
});

export default connect(mapStateToProps)(enhance(UserMenu));
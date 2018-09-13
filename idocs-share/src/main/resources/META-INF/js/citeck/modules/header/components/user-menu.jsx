import React from 'react';
import { compose, lifecycle, withState } from 'recompose';
import { Dropdown, Image } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/components/dropdown-menu-item';
import CustomToggle from 'js/citeck/modules/header/components/dropdown-menu-custom-toggle';
import { getPhotoSize } from 'js/citeck/modules/header/misc/api';

const UserMenu = ({ userName, userNodeRef, userPhotoUrl, items }) => {
    const userImage = userPhotoUrl ? (
        (
            <div className="user-photo-header">
                <div style={{backgroundImage: 'url(' + userPhotoUrl + ')'}}></div>
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
            <Dropdown className="user-dropdown-menu" pullRight>
                <CustomToggle bsRole="toggle" className="user-dropdown-menu__toggle">
                    <span className="user-menu-username">{userName}</span>
                    {userImage}
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const enhance = compose(
    withState('userPhotoUrl', 'setUserPhotoUrl', ''),
    lifecycle({
        componentDidMount() {
            const { userNodeRef, setUserPhotoUrl } = this.props;
            if (userNodeRef && getPhotoSize(userNodeRef)) {
                let photoUrl = Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/" + userNodeRef.replace(":/", "") + "/image.jpg";
                setUserPhotoUrl(photoUrl);
            }
        }
    }),
);

export default enhance(UserMenu);
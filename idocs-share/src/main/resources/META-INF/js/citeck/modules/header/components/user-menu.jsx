import React from 'react';
import { Dropdown, Image } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/components/dropdown-menu-item';
import CustomToggle from 'js/citeck/modules/header/components/dropdown-menu-custom-toggle';

const UserMenu = ({ userName, userNodeRef, items }) => {
    let userImage = null;
    if (userNodeRef) {
        let photoUrl = Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/" + userNodeRef.replace(":/", "") + "/image.jpg";
        console.log(photoUrl);
    }

    // TODO delete
    const defaultPhoto = 'https://citeck.ecos24.ru/share/proxy/alfresco/api/node/content;ecos:photo/workspace/SpacesStore/b4aa07be-b00a-46f8-9e62-9a7a93b08461/image.jpg';
    userImage = (
        <div className="user-photo-header">
            <div style={{backgroundImage: 'url(' + defaultPhoto + ')'}}></div>
        </div>
    );

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

export default UserMenu;
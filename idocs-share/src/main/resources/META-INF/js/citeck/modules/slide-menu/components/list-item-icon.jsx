import React from 'react';

const ListItemIcon = ({ item }) => {
    let itemId = item.id;
    let icon = <i className={`fa fa-menu-default-icon ${itemId}`} />;

    if (item.icon) {
        switch (item.icon.type) {
            case 'fa':
                icon = <i className={`fa fa-menu-default-icon ${item.icon.value}`} />;
                break;
            default:
                break;
        }
    }

    return icon;
};

export default ListItemIcon;


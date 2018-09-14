import React from 'react';
import DropDownMenuItem from 'js/citeck/modules/header/components/dropdown-menu-item';
import { t } from 'js/citeck/modules/header/misc/util';

const DropDownMenuGroup = ({ label, items }) => {
    const groupItems = items && items.length > 0 ? items.map((item, key) => {
        return (
            <DropDownMenuItem
                key={key}
                targetUrl={item.targetUrl}
                image={item.image}
                icon={item.icon}
                label={item.label}
                target={item.target}
            />
        );
    }) : null;

    return (
        <div className='custom-dropdown-menu-group'>
            <p className="custom-dropdown-menu-group__label">{t(label)}</p>
            {groupItems}
        </div>
    )
};

export default DropDownMenuGroup;
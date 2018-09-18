import React from 'react';
import DropDownMenuItem from './dropdown-menu-item';
import { t } from '../misc/util';

const DropDownMenuGroup = ({ id, label, items }) => {
    const groupItems = items && items.length > 0 ? items.map((item, key) => {
        return (
            <DropDownMenuItem
                key={key}
                id={item.id}
                targetUrl={item.targetUrl}
                image={item.image}
                icon={item.icon}
                label={item.label}
                target={item.target}
            />
        );
    }) : null;

    return (
        <div id={id} className='custom-dropdown-menu-group'>
            <p className="custom-dropdown-menu-group__label">{t(label)}</p>
            {groupItems}
        </div>
    )
};

export default DropDownMenuGroup;
import React from "react";
import { pure } from 'recompose';
import { MenuItem } from 'react-bootstrap';
import { t } from 'js/citeck/modules/header/misc/util';
import handleControl from '../misc/handle-control';

const DropDownMenuItem = ({ key, data }) => {
    const { id, targetUrl, iconClass, label, target, control } = data;
    // TODO rid of iconClass

    let clickHandler = null;
    if (control && control.type) {
        clickHandler = function (event, element) {
            event.preventDefault();
            handleControl(control.type, control.payload);
        };
    }

    return (
        <MenuItem
            eventKey={key}
            href={targetUrl}
            target={target}
            id={id}
            className="custom-dropdown-menu__item"
            onClick={clickHandler}
        >
            {iconClass && <i className={"fa " + iconClass} />}
            {label && t(label)}
        </MenuItem>
    )
};

export default pure(DropDownMenuItem);
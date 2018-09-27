import React from "react";
import { pure } from 'recompose';
import { MenuItem } from 'react-bootstrap';
import { t } from 'js/citeck/modules/header/misc/util';
import handleControl from '../misc/handle-control';

const DropDownMenuItem = ({ key, data }) => {
    const { id, targetUrl, label, target, control } = data;

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
            <i className={"fa fa-custom fa-custom__" + id} />
            {label && t(label)}
        </MenuItem>
    )
};

export default pure(DropDownMenuItem);
import React from "react";
import { pure } from 'recompose';
import { MenuItem, Image } from 'react-bootstrap';
import "xstyle!js/citeck/modules/header/components/dropdown-menu-item.css";

const DropDownMenuItem = ({id, key, targetUrl, image, icon, label, clickEvent, targetUrlType, targetUrlLocation}) => (
    <MenuItem
        eventKey={key}
        href={targetUrl}
        id={id}
        className="dropdown-menu-item"
        onClick={eval('(' + clickEvent + ')')}
    >
        {image && <Image src={image} thumbnail />}
        {icon && <i className={"fa " + icon} />}
        {label && Alfresco.util.message(label)}
    </MenuItem>
);

export default pure(DropDownMenuItem);
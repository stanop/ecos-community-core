import React from "react";
import { pure } from 'recompose';
import { MenuItem, Image } from 'react-bootstrap';
import "xstyle!js/citeck/modules/header/components/dropdown-menu-item.css";

const DropDownMenuItem = ({id, key, targetUrl, image, icon, label, clickEvent, target}) => (
    <MenuItem
        eventKey={key}
        href={targetUrl}
        target={target}
        id={id}
        className="custom-dropdown-menu__item"
        onClick={eval('(' + clickEvent + ')')} // TODO
    >
        {image && <Image src={image} />}
        {icon && <i className={"fa " + icon} />}
        {label && window.Alfresco.util.message(label)}
    </MenuItem>
);

export default pure(DropDownMenuItem);
import React, { PureComponent } from "react";
import { MenuItem, Image } from 'react-bootstrap';

import "xstyle!js/citeck/modules/header/base/dropdown-menu-item.css";

class DropDownMenuItem extends PureComponent {

    render() {

        const {id, key, targetUrl, image, icon, label, clickEvent, targetUrlType, targetUrlLocation} = this.props;

        return (
            <MenuItem eventKey={key}
                      href={targetUrl}
                      id={id}
                      className="dropdown-menu-item"
                      onClick={eval('(' + clickEvent + ')')}>
                {image && <Image src={image} thumbnail />}
                {icon && <i className={"fa " + icon} />}
                {label && Alfresco.util.message(label)}
            </MenuItem>
        );
    }
}
export default DropDownMenuItem;
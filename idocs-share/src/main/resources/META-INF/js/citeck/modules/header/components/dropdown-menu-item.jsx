import React from "react";
import { pure } from 'recompose';
import { MenuItem, Image } from 'react-bootstrap';
import { t } from 'js/citeck/modules/header/misc/util';

const DropDownMenuItem = ({ key, data }) => {
    const { id, targetUrl, image, iconClass, label, clickEvent, target, publishTopic, publishPayload } = data;

    let clickHandler = null;
    if (clickEvent) {
        clickHandler = eval('(' + clickEvent + ')');
    }

    if (publishTopic) {
        clickHandler = function (event, element) {
            event.preventDefault();
            console.log('publishTopic', publishTopic);
            console.log('publishPayload', publishPayload);
            // TODO
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
            {image && <Image src={image} />}
            {iconClass && <i className={"fa " + iconClass} />}
            {label && t(label)}
        </MenuItem>
    )
};

export default pure(DropDownMenuItem);
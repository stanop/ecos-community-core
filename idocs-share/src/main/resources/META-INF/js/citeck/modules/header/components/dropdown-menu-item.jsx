import React from "react";
import { connect } from "react-redux";
import { pure } from 'recompose';
import { MenuItem } from 'react-bootstrap';
import { t } from '../misc/util';
import handleControl from '../misc/handle-control';

const mapDispatchToProps = dispatch => ({
    dispatch
});

const DropDownMenuItem = ({ key, data, dispatch }) => {
    const { id, targetUrl, label, target, control } = data;

    let clickHandler = null;
    if (control && control.type) {
        clickHandler = event => {
            event.preventDefault();
            handleControl(control.type, control.payload, dispatch);
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

export default connect(null, mapDispatchToProps)(pure(DropDownMenuItem));
import React from "ecosui!react";
import { connect } from "ecosui!react-redux";
import { t } from '../../common/util';
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
        <li className="custom-dropdown-menu__item">
            <a
                // eventKey={key}
                href={targetUrl}
                target={target}
                id={id}
                onClick={clickHandler}
            >
                <i className={'fa fa-custom fa-custom__' + id} />
                {label && t(label)}
            </a>
        </li>
    );
};

export default connect(null, mapDispatchToProps)(DropDownMenuItem);
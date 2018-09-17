import React from 'react';
import { connect } from 'react-redux';
import { compose, lifecycle } from 'recompose';
import { Dropdown } from 'react-bootstrap';
import CustomToggle from './dropdown-menu-custom-toggle';
import DropDownMenuGroup from './dropdown-menu-group';
import { loadCreateCaseWidgetItems } from "../actions";

const CreateCaseWidget = ({ items }) => {
    const menuListItems = items && items.length > 0 ? items.map((item, key) => {
        return (
            <DropDownMenuGroup
                key={key}
                label={item.label}
                items={item.items}
            />
        );
    }) : null;

    return (
        <div id='HEADER_CREATE_CASE'>
            <Dropdown className="custom-dropdown-menu" pullLeft>
                <CustomToggle bsRole="toggle" className="create-case-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <i className={"fa fa-plus"} />
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const enhance = compose(
    lifecycle({
        componentDidMount() {
            const { userName, dispatch } = this.props;
            dispatch(loadCreateCaseWidgetItems(userName));
        }
    }),
);

const mapStateToProps = (state, ownProps) => ({
    items: state.caseMenu.items,
    userName: state.user.name,
});

export default connect(mapStateToProps)(enhance(CreateCaseWidget));

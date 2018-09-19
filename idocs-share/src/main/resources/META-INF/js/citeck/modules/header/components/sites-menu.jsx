import React from 'react';
import { connect } from 'react-redux';
import { compose, lifecycle } from 'recompose';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';
import { loadSiteMenuItems } from "../actions";

const SitesMenu = ({ items, headerTitle, headerIcon }) => {
    if (!Array.isArray(items) || items.length < 1) {
        return null;
    }

    const menuListItems = items.map((item, key) => (
        <DropDownMenuItem
            key={key}
            data={item.config}
        />
    ));

    return (
        <div id="HEADER_SITE_MENU">
            <Dropdown className="custom-dropdown-menu" pullRight>
                <CustomToggle bsRole="toggle" className="site-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <i className={"fa fa-cog"} />
                    {headerTitle}
                </CustomToggle>
                <Dropdown.Menu className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    );
};

const enhance = compose(
    lifecycle({
        componentDidMount() {
            const { siteId, userName, dispatch } = this.props;
            if (siteId && userName) {
                dispatch(loadSiteMenuItems(siteId, userName));
            }
        }
    }),
);

const mapStateToProps = (state, ownProps) => ({
    userName: state.user.name,
    siteId: state.siteMenu.id,
    items: state.siteMenu.items
});

export default connect(mapStateToProps)(enhance(SitesMenu));
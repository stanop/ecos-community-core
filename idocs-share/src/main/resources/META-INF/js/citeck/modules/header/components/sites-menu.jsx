import React from 'react';
import { connect } from 'react-redux';
import { compose, lifecycle } from 'recompose';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from './dropdown-menu-item';
import CustomToggle from './dropdown-menu-custom-toggle';
import { loadSiteData } from "../actions";

const SitesMenu = ({ items, headerTitle, headerIcon }) => {
    if (!items) {
        return null;
    }

    const menuListItems = items && items.length && items.map((item, key) => (
        <DropDownMenuItem
            key={key}
            targetUrl={item.targetUrl}
            image={item.image}
            icon={item.icon}
            label={item.label}
        />
    ));

    return (
        <div id="HEADER_SITE_MENU">
            <Dropdown className="custom-dropdown-menu" pullLeft>
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
            const { siteName, userName, dispatch } = this.props;
            dispatch(loadSiteData(siteName, userName));
        }
    }),
);

const mapStateToProps = (state, ownProps) => ({
    userName: state.user.name,
    siteName: state.siteMenu.name
});

export default connect(mapStateToProps)(enhance(SitesMenu));
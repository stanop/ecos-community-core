import React from 'react';
import { Dropdown } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/base/dropdown-menu-item';

export default class SitesMenu extends React.Component {
    render() {
        const {items, headerTitle, headerIcon} = this.props;
        let icon = headerIcon || "fa-cog";

        return (
            <div id="HEADER_SITE_MENU">
                <Dropdown pullRight>
                    <Dropdown.Toggle pullRight className="menu-item">
                        {<i className={"fa " + icon} />}
                        {headerTitle}
                    </Dropdown.Toggle>
                    <Dropdown.Menu>
                        {items && items.length && items.map((item, key) => {
                            return (
                                <DropDownMenuItem
                                    key={key}
                                    targetUrl={item.targetUrl}
                                    image={item.image}
                                    icon={item.icon}
                                    label={item.label}
                                />
                            )
                        })}
                    </Dropdown.Menu>
                </Dropdown>
            </div>
        );
    }
}
import React from 'react';
import { Dropdown, Image } from 'react-bootstrap';
import DropDownMenuItem from 'js/citeck/modules/header/base/dropdown-menu-item';

export default class UserMenu extends React.Component {

    render() {
        const {userName, items} = this.props;

        let userNodeRef = 'workspace://SpacesStore/b0da103f-e3c6-401f-9099-4e0087532b83';
        let photo = Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/" + userNodeRef.replace(":/", "") + "/image.jpg";

        return <div id='HEADER_USER_MENU'>
                    <Dropdown pullRight>
                        <Dropdown.Toggle className="menu-item">
                            {userName}
                            {/*<div className="user-photo-header" style={{float: 'right'}}>*/}
                                {/*<div style={{backgroundImage: 'url(' + photo + ')'}}></div>*/}
                            {/*</div>*/}
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
                </div>;
    }
}
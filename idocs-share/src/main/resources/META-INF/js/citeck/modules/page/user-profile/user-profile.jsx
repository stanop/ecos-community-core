import React from "ecosui!react";
import ReactDOM from "ecosui!react-dom";
import { UserProfileRoot } from './user-profile-components';
import { UserProfileProvider } from './user-profile-context';
import { DOCUMENTS_TAB, PROFILE_TAB } from './user-profile-util';

import "xstyle!./user-profile.css";

export function render(elementId, props) {
    const tabs = [
        {
            id: PROFILE_TAB,
            title: Alfresco.util.message('user-profile.tab.profile.label'),
        },
        {
            id: DOCUMENTS_TAB,
            title: Alfresco.util.message('user-profile.tab.documents.label'),
        }
    ];

    ReactDOM.render(
        <UserProfileProvider
            tabs={tabs}
            rootProps={props}
        >
            <UserProfileRoot {...props} />
        </UserProfileProvider>,
        document.getElementById(elementId)
    );
}

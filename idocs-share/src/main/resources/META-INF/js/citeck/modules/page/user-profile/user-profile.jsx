import React, { useContext, useState, useEffect } from "react";
import ReactDOM from "react-dom";
import SurfRegion from "../../surf/surf-region";
import { utils as CiteckUtils } from 'js/citeck/modules/utils/citeck';

require("xstyle!./user-profile.css");

const DEFAULT_TAB = "profile";

function getCurrentTab() {
    return CiteckUtils.getURLParameterByName("tab") || DEFAULT_TAB;
}

const UserProfileContext = React.createContext();

const UserProfileProvider = (props) => {
    const [currentTab, setCurrentTab] = useState(getCurrentTab());
    const [tabs, setTabs] = useState([]);

    // useEffect(() => {
    //     CiteckUtils.setURLParameter("tab", currentTab);
    // }, [currentTab]);

    useEffect(() => {
        window.onpopstate = function() {
            setCurrentTab(getCurrentTab());
        };

        return () => {
            window.onpopstate = () => {}
        };
    });

    useEffect(() => {
        setTabs(props.tabs.map(tab => {
            return {
                ...tab,
                isActive: tab.id === currentTab,
            }
        }));
    }, [props.tabs, currentTab]);

    return (
        <UserProfileContext.Provider
            value={{
                currentTab,
                tabs,

                setCurrentTab: tabId => {
                    setCurrentTab(tabId);
                },
            }}
        >
            {props.children}
        </UserProfileContext.Provider>
    );
};

const EditUserForm = ({ userRef, label, mode, theme }) => {
    let className = 'user-profile__edit-form_mode-edit';
    let editLink = null;

    if (mode === "view") {
        className = 'user-profile__edit-form_mode-view';
        editLink = <a className="user-profile__edit-link" href={'/share/page/user/admin/profile?mode=edit'}>{label}</a>;
    }

    return (
        <div className={className}>
            <SurfRegion
                args={{
                    regionId: mode === 'edit' ? 'node-edit' : 'node-view',
                    scope: 'page',
                    pageid: 'user-profile',
                    nodeRef: userRef,
                    htmlid: 'cardlet-remote-node-view',
                    mode: mode,
                    theme: theme,
                    // cacheAge: 600
                }}
            />
            {editLink}
        </div>
    );
};

const Documents = ({ userRef, theme }) => {
    return (
        <div className='user-profile-documents-tab'>
            <div className='user-profile-documents-tab__case-documents'>
                <SurfRegion
                    args={{
                        regionId: 'case-documents',
                        scope: 'page',
                        pageid: 'card-details',
                        nodeRef: userRef,
                        htmlid: 'cardlet-remote-case-documents',
                        theme: theme,
                        cacheAge: 600

                    }}
                />
            </div>
            <div className='user-profile-documents-tab__case-levels'>
                <SurfRegion
                    args={{
                        regionId: 'case-levels',
                        scope: 'page',
                        pageid: 'card-details',
                        nodeRef: userRef,
                        htmlid: 'cardlet-remote-case-levels',
                        theme: theme,
                        cacheAge: 600,
                    }}
                />
            </div>
        </div>
    );
};

const Tab = (props) => {
    const context = useContext(UserProfileContext);
    const onClick = () => {
        context.setCurrentTab(props.id);
        CiteckUtils.setURLParameter("tab", props.id);
    };

    let className = "header-tab";
    if (props.isActive) {
        className += " current";
    }

    return (
        <span className={className}>
            <a onClick={onClick}>{props.title}</a>
        </span>
    );
};

const UserProfileRoot = ({ el, userRef, label, mode, theme }) => {
    const context = useContext(UserProfileContext);
    // console.log('context', context);

    return (
        <div id={`${el}-body`} className="user-profile node-view static user-profile-wrapper">
            <div id="profile-view">

                <div id="card-details-tabs" className="header-tabs">
                    {context.tabs.map(tab => {
                        return <Tab key={`card-mode-link-${tab.id}`} {...tab} />
                    })}
                </div>
                {context.currentTab === 'documents' ? (
                    <Documents userRef={userRef} theme={theme} />
                ) : (
                    <EditUserForm
                        userRef={userRef}
                        label={label}
                        mode={mode}
                        theme={theme}
                    />
                )}
            </div>
        </div>
    );
};

export function render(elementId, props) {
    // console.log('props', props);
    const tabs = [
        {
            id: "profile",
            title: Alfresco.util.message('user-profile.tab.profile.title'),
        },
        {
            id: "documents",
            title: Alfresco.util.message('user-profile.tab.documents.title'),
        }
    ];

    ReactDOM.render(
        <UserProfileProvider
            tabs={tabs}
        >
            <UserProfileRoot {...props} />
        </UserProfileProvider>,
        document.getElementById(elementId)
    );
}

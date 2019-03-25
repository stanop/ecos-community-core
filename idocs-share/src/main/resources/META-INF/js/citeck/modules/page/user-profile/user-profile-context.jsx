import React, { useState, useEffect } from "react";
import { getCurrentTab } from './user-profile-util';

export const UserProfileContext = React.createContext();

export const UserProfileProvider = (props) => {
    const [currentTab, setCurrentTab] = useState(getCurrentTab());
    const [tabs, setTabs] = useState([]);

    useEffect(() => {
        window.onpopstate = function() {
            setCurrentTab(getCurrentTab());
        };

        return () => {
            window.onpopstate = () => {}
        };
    }, []);

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
                rootProps: {
                    ...props.rootProps,
                },
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
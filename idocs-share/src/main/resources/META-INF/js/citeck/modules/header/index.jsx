import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, compose } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'js/citeck/lib/redux-thunk';

import ShareHeader from './share-header';
import API from './misc/api';
import rootReducer from './reducers';
import {
    setUserFullName,
    loadTopMenuData,
    loadUserMenuPhoto
} from './actions';

import "xstyle!js/citeck/lib/css/bootstrap.min.css";

// TODO include polyfills

const api = new API(window.Alfresco.constants.PROXY_URI);

let composeEnhancers = compose;
if (typeof window === 'object' && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
    composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
}

const store = createStore(rootReducer, {}, composeEnhancers(
        applyMiddleware(thunk.withExtraArgument(api)),
    )
);

export const render = (elementId, props) => {
    store.dispatch(setUserFullName(props.userFullname));
    store.dispatch(loadUserMenuPhoto(props.userNodeRef));

    store.dispatch(loadTopMenuData(
        props.userName,
        props.userIsAvailable === "true",
        props.userIsMutable === "true",
        props.isExternalAuthentication === "true",
        props.siteMenuItems
    ));

    ReactDOM.render(
        <Provider store={store}>
            <ShareHeader { ...props } />
        </Provider>,
        document.getElementById(elementId)
    );

    if (props.slideMenuConfig) {
        require(['js/citeck/header/citeckMainSlideMenu'], function(CiteckMainSlideMenu) {
            new CiteckMainSlideMenu(props.slideMenuConfig);
        });
    }
};

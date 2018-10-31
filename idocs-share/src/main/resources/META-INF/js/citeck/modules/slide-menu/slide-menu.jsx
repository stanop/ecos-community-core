import React from 'react';
import ReactDOM from "react-dom";
import { createStore, applyMiddleware, compose } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'js/citeck/lib/redux-thunk';
import SlideMenu from "./components/slide-menu";
import rootReducer from './reducers';
import API from '../common/api';
import {
    setSmallLogo,
    setLargeLogo,
    setLeftMenuItems,
    setLeftMenuExpandableItems,
    setSelectedId
} from './actions';
import {
    selectedMenuItemIdKey,
    fetchExpandableItems,
    processApiData
} from './util';
import "xstyle!./slide-menu.css";

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
    // TODO use api
    new Promise(resolve => {
        const apiData = processApiData(props.slideMenuConfig.widgets);
        // console.log(apiData);
        resolve(apiData);
    }).then(menuItems => {
        let selectedId = null;
        if (sessionStorage && sessionStorage.getItem) {
            selectedId = sessionStorage.getItem(selectedMenuItemIdKey);
            store.dispatch(setSelectedId(selectedId));
        }

        const expandableItems = fetchExpandableItems(menuItems, selectedId);

        store.dispatch(setLeftMenuItems(menuItems));
        store.dispatch(setLeftMenuExpandableItems(expandableItems));
    });

    // TODO use api
    store.dispatch(setSmallLogo(props.slideMenuConfig.logoSrcMobile));
    store.dispatch(setLargeLogo(props.slideMenuConfig.logoSrc));

    ReactDOM.render(
        <Provider store={store}>
            <SlideMenu />
        </Provider>,
        document.getElementById(elementId)
    );
};
import React from 'ecosui!react';
import ReactDOM from "ecosui!react-dom";
import { createStore, applyMiddleware, compose } from 'ecosui!redux';
import { Provider } from 'ecosui!react-redux';
import thunk from 'ecosui!redux-thunk';
import SlideMenu from "./components/slide-menu";
import rootReducer from './reducers';
import API from '../common/api';
import {
    setSmallLogo,
    setLargeLogo,
    setLeftMenuItems,
    setLeftMenuExpandableItems,
    setSelectedId,
    setIsReady,
    setNewJournalsPageEnable
} from './actions';
import {
    selectedMenuItemIdKey,
    fetchExpandableItems
} from './util';
import "xstyle!./slide-menu.css";

const api = new API(window.Alfresco.constants.PROXY_URI);

// let composeEnhancers = compose;
// if (typeof window === 'object' && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
//     composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
// }

const store = createStore(rootReducer, {}, compose(
        applyMiddleware(thunk.withExtraArgument(api)),
    )
);

export const render = (elementId, props) => {
    api.getNewJournalsPageEnable()
        .then(function(isEnable){
            store.dispatch(setNewJournalsPageEnable(isEnable));
        })
        .catch(() => {});

    api.getSlideMenuItems().then(apiData => {
        const slideMenuData = apiData.items;
        // console.log('slideMenuData', slideMenuData);

        let selectedId = null;
        if (sessionStorage && sessionStorage.getItem) {
            selectedId = sessionStorage.getItem(selectedMenuItemIdKey);
            store.dispatch(setSelectedId(selectedId));
        }

        const expandableItems = fetchExpandableItems(slideMenuData, selectedId);

        store.dispatch(setLeftMenuItems(slideMenuData));
        store.dispatch(setLeftMenuExpandableItems(expandableItems));
        store.dispatch(setIsReady(true));
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
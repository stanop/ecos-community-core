import React from 'react';
import ReactDOM from "react-dom";
import { createStore, applyMiddleware, compose } from 'redux';
import { Provider } from 'react-redux';
import thunk from 'js/citeck/lib/redux-thunk';
import SlideMenu from "./components/slide-menu";
import rootReducer from './reducers';
import {
    setSmallLogo,
    setLargeLogo,
    setLeftMenuItems,
    setLeftMenuExpandableItems,
    setSelectedId
} from './actions';
import "xstyle!./slide-menu.css";

const selectedMenuItemIdKey = 'selectedMenuItemId';

let composeEnhancers = compose;
if (typeof window === 'object' && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__) {
    composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
}

const api = null; // TODO
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

// TODO delete
// const itemId = item.id; // .split(' ').join('_'); TODO
function processApiData(oldItems) {
    if (!oldItems) {
        return null;
    }

    return oldItems.map(item => {
        let newItem = { ...item };
        delete newItem['widgets'];
        if (item.widgets) {
            newItem.items = processApiData(item.widgets);
        }

        return newItem;
    });
}

function fetchExpandableItems(items, selectedId) {
    let flatList = [];
    items.map(item => {
        const hasNestedList = !!item.items;
        if (hasNestedList) {
            let isNestedListExpanded = !!item.sectionTitle || hasChildWithId(item.items, selectedId);
            flatList.push(
                {
                    id: item.id,
                    hasNestedList,
                    isNestedListExpanded,
                },
                ...fetchExpandableItems(item.items, selectedId)
            );
        }
    });

    return flatList;
}

function hasChildWithId(items, selectedId) {
    let childIndex = items.findIndex(item => item.id === selectedId);
    if (childIndex !== -1) {
        return true;
    }

    let totalItems = items.length;

    for (let i = 0; i < totalItems; i++) {
        if (!items[i].items) {
            continue;
        }

        let hasChild = hasChildWithId(items[i].items, selectedId);
        if (hasChild) {
            return true;
        }
    }

    return false;
}
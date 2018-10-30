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
    setLeftMenuFlatItems
} from './actions';
import "xstyle!./slide-menu.css";

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
    const flatList = fetchFlatList(props.slideMenuConfig.widgets);

    store.dispatch(setLeftMenuItems(props.slideMenuConfig.widgets));
    store.dispatch(setLeftMenuFlatItems(flatList));

    store.dispatch(setSmallLogo(props.slideMenuConfig.logoSrcMobile));
    store.dispatch(setLargeLogo(props.slideMenuConfig.logoSrc));

    ReactDOM.render(
        <Provider store={store}>
            <SlideMenu />
        </Provider>,
        document.getElementById(elementId)
    );
};

// const itemId = item.id; // .split(' ').join('_'); TODO

// TODO rename
function fetchFlatList(items) {
    let flatList = [];
    items.map(item => {
        const hasNestedList = !!item.widgets;
        if (hasNestedList) {
            let isNestedListExpanded = !!item.sectionTitle; // TODO or hasChildId
            flatList.push(
                {
                    id: item.id,
                    hasNestedList,
                    isNestedListExpanded,
                },
                ...fetchFlatList(item.widgets)
            );
        }
    });

    return flatList;
}
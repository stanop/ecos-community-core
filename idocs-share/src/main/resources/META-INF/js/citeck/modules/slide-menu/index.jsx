import React from 'react';
import ReactDOM from 'react-dom';
import SlideMenu from './slide-menu';

export const render = (elementId, props) => {
    ReactDOM.render(
        <SlideMenu { ...props } />,
        document.getElementById(elementId)
    );
};

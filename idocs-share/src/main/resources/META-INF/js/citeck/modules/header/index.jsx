import React from 'react';
import ReactDOM from 'react-dom';
import ShareHeader from 'js/citeck/modules/header/share-header';

export const render = (elementId, props) => {
    ReactDOM.render(
        <ShareHeader { ...props } />,
        document.getElementById(elementId)
    );
};

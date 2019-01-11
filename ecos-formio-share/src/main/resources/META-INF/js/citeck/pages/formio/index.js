//import React from 'react';
//import ReactDOM from 'react-dom';
//import FormioWrapper from './formio_wrapper.js';
//import $ from 'jquery';

window.onload = function() {
    /*Formio.builder(document.getElementById('formio'), {components: [
        {
            type: 'textfield',
            label: 'First Name',
            key: 'firstName',
            input: true
        }
    ]});*/
    Formio.createForm(document.getElementById('formio'), 'https://examples.form.io/example');
};

//$(document).ready(function() {
    /*ReactDOM.render(
        React.createElement(FormioWrapper),
        document.getElementById('formio-root')
    );*/
//});

//import React from 'react';
//import ReactDOM from 'react-dom';
//import FormioWrapper from './formio_wrapper.js';
//import $ from 'jquery';

/* public String formType;
        public String formKey;
        public String formId;
        public String formMode;*/

import records from 'js/citeck/modules/records/records';

function getForm(record, formKey) {

    return fetch('/share/proxy/alfresco/citeck/ecos/records/query', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify({
            query: {
                sourceId: 'formio',
                query: {
                    record: record,
                    formKey: formKey
                }
            },
            attributes: {
                submission: '.att(n:"submission"){att(n:"data"){json}}',
                formDef: 'definition?json'
            },
        })
    }).then(response => { return response.json();});
}

function getFormInputs(root, inputs) {

    if (!inputs) {
        inputs = [];
    }

    let components = root.components || [];

    for (let i = 0; i < components.length; i++) {
        let component = components[i];
        if (component.component.input === true) {
            inputs.push(component);
        }
        getFormInputs(component, inputs);
    }

    return inputs;
}

window.onload = function() {

    var urlString = window.location.href;
    var url = new URL(urlString);

    var record = url.searchParams.get("record");
    var formKey = url.searchParams.get("formKey");

    getForm(record, formKey).then(data => {

        var formAtts = data.records[0].attributes;

        Formio.createForm(document.getElementById('formio'), formAtts.formDef).then(form => {

            form.submission = {
                data: formAtts.submission
            };

            window.formioForm = form;

            form.on('submit', (submission) => {

                let inputs = getFormInputs(form);

                let attributes = {};
                for (let i = 0; i < inputs.length; i++) {

                    let input = inputs[i].component;
                    if (input.type == 'button') {
                        continue;
                    }
                    let attribute = (input.properties || {}).attribute || input.key;

                    attributes[attribute] = submission.data[input.key];
                }

                fetch('/share/proxy/alfresco/citeck/ecos/records/mutate', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-type': 'application/json;charset=UTF-8'
                    },
                    body: JSON.stringify({
                        record: {
                            id: record,
                            attributes: attributes
                        }
                    })
                }).then(response => { return response.json();})
                    .then(record => {
                        if (record.id) {
                            window.location = '/share/page/card-details?nodeRef=' + record.id;
                        }
                    });

            });
        });
    });

    /*Formio.builder(document.getElementById('formio'), {components: [
        {
            type: 'textfield',
            label: 'First Name',
            key: 'firstName',
            input: true
        }
    ]});*/

};

//$(document).ready(function() {
    /*ReactDOM.render(
        React.createElement(FormioWrapper),
        document.getElementById('formio-root')
    );*/
//});

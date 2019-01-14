//import React from 'react';
//import ReactDOM from 'react-dom';
//import FormioWrapper from './formio_wrapper.js';
//import $ from 'jquery';

/* public String formType;
        public String formKey;
        public String formId;
        public String formMode;*/

function getForm(formType, formKey, formId, formMode) {

    let query = {};

    if (formType) {
        query.formType = formType;
    }

    if (formKey) {
        query.formKey = formKey;
    }

    if (formId) {
        query.formId = formId;
    }

    if (formMode) {
        query.formMode = formMode;
    }

    return fetch('/share/proxy/alfresco/citeck/ecos/records/query', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify({
            query: {
                sourceId: 'formio',
                query: query
            },
            attributes: {formDef: 'definition?json'},
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

    var formType = url.searchParams.get("formType");
    var formKey = url.searchParams.get("formKey");
    var formMode = url.searchParams.get("formMode");
    var formId = url.searchParams.get("formId");

    getForm(formType, formKey, formMode, formId).then(data => {

        var definition = data.records[0].attributes.formDef;

        Formio.createForm(document.getElementById('formio'), definition).then(form => {

            fetch('/share/proxy/alfresco/citeck/ecos/records/query', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-type': 'application/json;charset=UTF-8'
                },
                body: JSON.stringify({
                    record: "workspace://SpacesStore/142b5c69-85e5-4b2e-a0e7-9bdf94ce3e51",
                    attributes: {
                        title: 'cm:title',
                        description: 'cm:description',
                        formType: 'ecosFormio:formType',
                        formKind: 'ecosFormio:formKind',
                        formId: 'ecosFormio:formId',
                        formMode: 'ecosFormio:formMode',
                    },
                })
            }).then(response => { return response.json();})
                .then(data => {
                form.submission = {
                        data: data.attributes
                    }
                }
            );

            window.formioForm = form;

            form.on('submit', (submission) => {

                let inputs = getFormInputs(form);

                let attributes = {};
                for (let i = 0; i < inputs.length; i++) {

                    let input = inputs[i];
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
                        sourceId: 'formio',
                        record: {
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

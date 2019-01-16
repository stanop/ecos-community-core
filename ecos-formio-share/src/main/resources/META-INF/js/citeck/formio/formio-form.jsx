import React from "react";
import { connect } from 'react-redux';

import "js/citeck/lib/formio/formio.full";

import "xstyle!https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css";
import "xstyle!https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css";
import "xstyle!js/citeck/lib/formio/formio.full.min.css";

var formCounter = 0;

export default class FormioForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            containerId: 'form-' + formCounter++
        }
    }

    componentDidMount() {

        let getFormInputs = function (root, inputs) {

            if (!inputs) {
                inputs = [];
            }

            let components = root.components || [];

            for (let i = 0; i < components.length; i++) {
                let component = components[i];
                let config = component.component;
                if (config.input === true && config.type !== "button") {
                    inputs.push(component);
                }
                getFormInputs(component, inputs);
            }

            return inputs;
        };

        this.getForm().then(data => {

            var formAtts = data.records[0].attributes;

            Formio.createForm(document.getElementById(this.state.containerId), formAtts.formDef).then(form => {

                form.submission = {
                    data: formAtts.submission
                };

                form.on('submit', (submission) => {

                    let inputs = getFormInputs(form);

                    let attributes = {};
                    for (let i = 0; i < inputs.length; i++) {

                        let input = inputs[i].component;
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
                            sourceId: this.props.sourceId,
                            record: {
                                id: this.props.record,
                                attributes: attributes
                            }
                        })
                    }).then(response => {
                        return response.json();
                    }).then(record => {
                        if (record.id) {
                            window.location = '/share/page/card-details?nodeRef=' + record.id;
                        }
                    });
                });
            });
        });
    }

    render() {
        return <div id={this.state.containerId} />
    }

    getForm() {

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
                        record: this.props.record,
                        formKey: this.props.formKey
                    }
                },
                attributes: {
                    submission: '.att(n:"submission"){att(n:"data"){json}}',
                    formDef: 'definition?json'
                },
            })
        }).then(response => { return response.json();});
    }
}
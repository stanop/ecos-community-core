import React from "react";
import { connect } from 'react-redux';

import "js/citeck/lib/formio/formio.full";
import Records from "js/citeck/modules/records/records";

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

        let self = this;

        this.getForm().then(data => {

            var formAtts = data.records[0].attributes;

            Formio.createForm(document.getElementById(this.state.containerId), formAtts.formDef).then(form => {

                form.ecos = {};
                form.ecos.Records = Records;
                form.ecos.record = Records.get(this.props.record);

                FormioForm.getData(form).then(data => {

                    form.submission = {
                        data: data
                    };

                    form.on('submit', (submission) => {
                        self.submitForm(form, submission);
                    });

                    if (this.props.onReady) {
                        this.props.onReady(form);
                    }
                });
            });
        });
    }

    submitForm(form, submission) {

        let inputs = FormioForm.getFormInputs(form);

        for (let i = 0; i < inputs.length; i++) {

            let input = inputs[i].component;
            let attribute = inputs[i].attribute;

            form.ecos.record.att(attribute, submission.data[input.key]);
        }

        form.ecos.record.save().then(record => {
            if (this.props.onSubmit) {
                this.props.onSubmit(record);
            }
        });
    }

    static getFormInputs(root, inputs) {

        if (!inputs) {
            inputs = [];
        }

        let components = root.components || [];

        for (let i = 0; i < components.length; i++) {
            let component = components[i];
            let config = component.component;
            if (config.input === true && config.type !== "button") {
                inputs.push({
                    attribute: (config.properties || {}).attribute || config.key,
                    component: component
                });
            }
            FormioForm.getFormInputs(component, inputs);
        }

        return inputs;
    };

    static getData(form) {

        if (!form.ecos.record) {
            return new Promise((success) => {
                success({});
            });
        }

        let inputs = FormioForm.getFormInputs(form);
        let attributes = {};
        for (let input of inputs) {
            attributes[input.component.component.key] = input.attribute;
        }

        return form.ecos.record.load(attributes);
    }

    render() {
        return <div id={this.state.containerId} />
    }

    getForm() {

        return Records.query({
            query: {
                sourceId: 'formio',
                query: {
                    record: this.props.record,
                    formKey: this.props.formKey
                }
            },
            attributes: {
                formDef: 'definition?json'
            }
        });
    }
}
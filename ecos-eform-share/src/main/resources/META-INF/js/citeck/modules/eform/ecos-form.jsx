import React from "react";
import {connect} from 'react-redux';

import {Form} from "js/citeck/lib/formio/formio.full";
import Records from "js/citeck/modules/records/records";
// import CustomComponents from "js/citeck/modules/eform/import/ecos-form-custom";

import "xstyle!https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css";
import "xstyle!js/citeck/lib/formio/formio.full.min.css";
import "xstyle!js/citeck/modules/eform/ecos-form.css";
import "xstyle!js/citeck/modules/eform/import/ecos-form-custom.css";

// for (let componentName in CustomComponents) {
//     if (CustomComponents.hasOwnProperty(componentName)) {
//         window.Formio.Components.addComponent(componentName, CustomComponents[componentName]);
//     }
// }

var formCounter = 0;

export default class EcosForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            containerId: 'form-' + formCounter++
        }
    }

    componentDidMount() {

        let self = this;

        this.getForm().then(data => {

            var formAtts = data.records[0].attributes,
                options = self.props.options || {};

            Formio.createForm(document.getElementById(this.state.containerId), formAtts.formDef, options).then(form => {
                let record = Records.get(self.props.record);
                form.ecos = {
                    record: record
                };

                let customModule = new Promise(function (resolve, reject) {
                    if (formAtts.customModule) {
                        require([formAtts.customModule], function (Module) {
                            resolve(new Module.default({
                                form: form,
                                record: record
                            }));
                        });
                    } else {
                        resolve({});
                    }
                });


                Promise.all([customModule, EcosForm.getData(record, form)]).then(data => {

                    form.ecos.custom = data[0];

                    form.submission = {
                        data: {
                            ...(self.props.attributes || {}),
                            ...data[1]
                        }
                    };

                    form.on('submit', (submission) => {
                        self.submitForm(form, submission);
                    });

                    let handlersPrefix = "onForm";

                    for (let key in self.props) {

                        if (self.props.hasOwnProperty(key) && key.startsWith(handlersPrefix)) {

                            let event = key.slice(handlersPrefix.length).toLowerCase();

                            if (event != 'submit') {
                                form.on(event, () => {
                                    self.props[key].apply(form, arguments);
                                });
                            } else {
                                console.warn("Please use onSubmit handler instead of onFormSubmit");
                            }
                        }
                    }

                    if (self.props.onReady) {
                        self.props.onReady(form);
                    }
                });
            });
        });
    }

    fireEvent(event, data) {
        var handlerName = 'on' + event.charAt(0).toUpperCase() + event.slice(1);
        if (this.props[handlerName]) {
            this.props[handlerName](data);
        }
    }

    submitForm(form, submission) {

        var self = this;

        let inputs = EcosForm.getFormInputs(form);
        let keysMapping = {};

        for (let i = 0; i < inputs.length; i++) {
            keysMapping[inputs[i].component.key] = inputs[i].attribute;
        }

        let record = form.ecos.record;

        for (let key in submission.data) {
            if (submission.data.hasOwnProperty(key)) {
                record.att(keysMapping[key] || key, submission.data[key]);
            }
        }

        form.ecos.record.save().then(record => {
            if (self.props.onSubmit) {
                self.props.onSubmit(record);
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
            EcosForm.getFormInputs(component, inputs);
        }

        return inputs;
    };

    static getData(record, form) {

        if (!record) {
            return new Promise((success) => {
                success({});
            });
        }

        let inputs = EcosForm.getFormInputs(form);
        let attributes = {};
        for (let input of inputs) {
            let key = input.component.component.key;
            if (key) {
                attributes[key] = input.attribute;
            }
        }

        return record.load(attributes);
    }

    render() {
        return <div id={this.state.containerId}/>
    }

    getForm() {

        return Records.query({
            query: {
                sourceId: 'eform',
                query: {
                    record: this.props.record,
                    formKey: this.props.formKey
                }
            },
            attributes: {
                formDef: 'definition?json',
                customModule: 'customModule'
            }
        });
    }
}
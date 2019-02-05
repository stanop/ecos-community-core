
let formPanelIdx = 0;

export default class FormioFormCustom {

    constructor(params) {

        this.form = params.form;

        var formId = 'formio-editor-form-panel' + formPanelIdx++;

        this.panel = new YAHOO.widget.Panel(formId, {
            width: "1200px",
            height: "auto",
            fixedcenter:  "contained",
            constraintoviewport: true,
            visible: false,
            //close: false, // Because not access to runtime
            modal: false,
            postmethod: "none", // Will make Dialogs not auto submit <form>s it finds in the dialog
            hideaftersubmit: false, // Will stop Dialogs from hiding themselves on submits
            fireHideShowEvents: true
        });

        this.panel.setHeader("");
        this.contentId = formId + '-content';
        this.panel.setBody('<div class="formio-panel-content" id="' + this.contentId + '"></div>' +
                           '<button id="' + this.contentId + '-submit" ' +
            '                       class="btn btn-default btn-md" type="button">Submit</button>');

        let children = document.body.children || [];
        this.panel.render(children[children.length - 1]);
    }

    showFormEditor() {

        let self = this;

        let processContent = function (formModel) {

            let model = JSON.parse(formModel);

            Formio.builder(document.getElementById(self.contentId), model).then(editorForm => {

                document.getElementById(self.contentId + '-submit').addEventListener("click", e => {
                    self.form.ecos.record.att('definition', JSON.stringify(editorForm.form));
                    self.panel.hide();
                });

                self.panel.show();
                self.panel.center();
            });
        };

        self.form.ecos.record.load({definition: 'definition'}).then(data => {

            if (!data.definition) {
                self.form.ecos.Records.query({
                    query: {
                        sourceId: 'formio',
                        query: {
                            formKey: 'DEFAULT'
                        }
                    },
                    attributes: ['definition']
                }).then(data => {
                    processContent(data.records[0].attributes.definition);
                });
            } else {
                processContent(data.definition);
            }
        });
    }
}

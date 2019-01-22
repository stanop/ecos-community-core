
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

        self.form.ecos.record.load({formModel: 'cm:content'}).then(data => {

            let model = JSON.parse(data.formModel);

            Formio.builder(document.getElementById(this.contentId), model.definition).then(editorForm => {

                document.getElementById(this.contentId + '-submit').addEventListener("click", e => {
                    self.panel.hide();

                    model.definition = editorForm.form;
                    self.form.ecos.record.att('cm:content', JSON.stringify(model));
                });

                self.panel.show();
                self.panel.center();
            });
        });
    }
}

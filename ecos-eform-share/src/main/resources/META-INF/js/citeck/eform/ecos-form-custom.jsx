
let formPanelIdx = 0;

export default class EcosFormCustom {

    constructor(params) {

        this.form = params.form;
        this.record = params.record;

        var formId = 'eform-editor-form-panel' + formPanelIdx++;

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
        this.panel.setBody('<div class="eform-panel-content" id="' + this.contentId + '"></div>' +
                           '<button id="' + this.contentId + '-submit" ' +
            '                       class="btn btn-default btn-md" type="button">Submit</button>');

        let children = document.body.children || [];
        this.panel.render(children[children.length - 1]);
    }

    isSystemForm() {
        return ['eform@DEFAULT', 'eform@ECOS_FORM'].indexOf(this.record.id) >= 0;
    }

    showFormEditor() {

        let self = this;

        let processContent = function (formModel) {

            Form.builder(document.getElementById(self.contentId), formModel).then(editorForm => {

                document.getElementById(self.contentId + '-submit').addEventListener("click", e => {
                    self.record.att('definition', editorForm.form);
                    self.panel.hide();
                });

                self.panel.show();
                self.panel.center();
            });
        };

        let defAtts = {
            definition: 'definition?json'
        };

        self.record.load(defAtts).then(data => {

            if (!data.definition) {
                Citeck.Records.get('eform@DEFAULT').load(defAtts).then(data => {
                    processContent(data.definition);
                });
            } else {
                processContent(data.definition);
            }
        });
    }
}

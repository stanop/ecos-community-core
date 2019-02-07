
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
        this.submitBtnId = this.contentId + '-submit';
        this.cancelBtnId = this.contentId + '-cancel';

        this.panel.setBody('<div class="eform-panel-content" id="' + this.contentId + '"></div>' +
                           '<div class="eform-panel-actions">' +
                               '<button id="' + this.submitBtnId + '" ' +
                                       'class="btn btn-default btn-md eform-edit-form-btn btn-primary" ' +
                                       'type="button">Save</button>' +
                               '<button id="' + this.cancelBtnId + '" ' +
                                       'class="btn btn-default btn-md eform-edit-form-btn" ' +
                                       'type="button">Cancel</button>' +
                           '</div>');

        let children = document.body.children || [];
        this.panel.render(children[children.length - 1]);
    }

    isSystemForm() {
        return ['eform@DEFAULT', 'eform@ECOS_FORM'].indexOf(this.record.id) >= 0;
    }

    showFormEditor() {

        let self = this;

        let processContent = function (formModel) {

            Formio.builder(document.getElementById(self.contentId), formModel).then(editorForm => {

                document.getElementById(self.submitBtnId).addEventListener("click", e => {
                    self.record.att('definition', editorForm.form);
                    self.panel.hide();
                });

                document.getElementById(self.cancelBtnId).addEventListener("click", e => {
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

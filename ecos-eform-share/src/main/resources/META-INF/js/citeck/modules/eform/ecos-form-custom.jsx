
export default class EcosFormCustom {

    constructor(params) {
        this.form = params.form;
        this.recordId = params.recordId;
    }

    isSystemForm() {
        return ['eform@DEFAULT', 'eform@ECOS_FORM'].indexOf(this.recordId) >= 0;
    }
}

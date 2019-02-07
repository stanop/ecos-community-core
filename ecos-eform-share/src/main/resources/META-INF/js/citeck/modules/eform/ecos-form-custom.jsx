
export default class EcosFormCustom {

    constructor(params) {
        this.form = params.form;
        this.record = params.record;
    }

    isSystemForm() {
        return ['eform@DEFAULT', 'eform@ECOS_FORM'].indexOf(this.record.id) >= 0;
    }
}

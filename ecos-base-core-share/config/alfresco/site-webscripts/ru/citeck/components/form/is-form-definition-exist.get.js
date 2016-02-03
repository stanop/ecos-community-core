
var type = args['type'];
var formId = args['formId'];
var formConfig = config.scoped[type].forms.getForm(formId);

model.isExist = (formConfig)? 'true' : 'false';
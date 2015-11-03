<#include "/ru/citeck/components/form/controls/common/key-value-picker.inc.ftl" />

<@renderKeyValuePickerJS field=field params = {
	"rootURL": "${url.context}/proxy/alfresco/api/classesWithFullQname?cf=type",
	"itemKey": field.control.params.itemKey!"name",
	"itemTitle": field.control.params.itemTitle!"{prefixedName} ({title})"
} />
<@renderKeyValuePickerHTML field />

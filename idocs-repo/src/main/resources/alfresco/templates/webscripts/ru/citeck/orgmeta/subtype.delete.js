<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgmeta/orgmeta.lib.js">
(function() {

var type = url.templateArgs.type,
	name = url.templateArgs.name;

// first, get the specified group sub-type
var subtype = orgmeta.getSubType(type, name);

if(subtype == null) {
	status.setCode(status.STATUS_NOT_FOUND, "Can't find group sub-type " + type + " - " + name);
	return;
}

// then, check all groups of this subtype:
var groups = orgstruct.getAllTypedGroups(type, name, false);
if(groups.length > 0) {
	status.setCode(500, "Can not delete group sub-type, while there are groups with this sub-type");
	return;
}

// actually delete sub-type:
orgmeta.deleteSubType(type, name);

model.success = true;
model.message = "Successfully deleted group sub-type: " + type + " - " + name;

})();

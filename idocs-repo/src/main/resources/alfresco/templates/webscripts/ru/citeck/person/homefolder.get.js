(function() {

var person = people.getPerson(url.templateArgs.userName);
if(person == null) {
	status.setCode(status.STATUS_NOT_FOUND, "Can't find person " + url.templateArgs.userName);
	return;
}

model.homefolder = person.properties["cm:homeFolder"];


})();
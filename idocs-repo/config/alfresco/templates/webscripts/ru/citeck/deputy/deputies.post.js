(function() {

var fullName = url.templateArgs.fullName;
var roleMode = fullName.match(/^GROUP_/);
var currentUserMode = fullName == person.properties.userName;

if(args.users == null || args.users == "") {
	status.setCode(status.STATUS_BAD_ARGUMENT, "users argument is mandatory");
	return;
}

var users = args.users.split(/,/);

if(roleMode) {
	deputies.addRoleDeputies(fullName, users);
} else if(currentUserMode) {
	deputies.addCurrentUserDeputies(users);
} else {
	users = deputies.addUserDeputies(fullName, users);
}

model.success = true;

})();
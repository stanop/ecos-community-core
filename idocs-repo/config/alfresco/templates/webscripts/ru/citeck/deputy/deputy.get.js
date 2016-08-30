(function() {

var fullName = url.templateArgs.fullName;
var userName = url.templateArgs.userName;

var user = groups.getUser(userName);
if(!user) {
	status.setCode(status.STATUS_NOT_FOUND, "No such user: " + userName);
	return;
}

if(fullName) {

var roleMode = fullName.match(/^GROUP_/);
var currentUserMode = fullName == person.properties.userName;

var deputied = false;
if(roleMode) {
	deputied = deputies.isRoleDeputiedToUser(fullName, userName);
} else {
	var users = [];
	if(currentUserMode) {
		users = deputies.getCurrentUserDeputies();
	} else {
		users = deputies.getUserDeputies(fullName);
	}
	for(var i in users) {
		if(users[i].userName == userName) {
			deputied = true;
			break;
		}
	}
}

if(!deputied) {
	status.setCode(status.STATUS_NOT_FOUND, "User " + userName + " is not deputy of " + fullName);
	return;
}

}

model.deputy = {
	"user": user,
	"available": availability.getUserAvailability(userName),
	"canDelete": true,
	"deputy": true
};

})();
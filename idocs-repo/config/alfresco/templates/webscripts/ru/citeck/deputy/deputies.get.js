(function() {

var fullName = url.templateArgs.fullName;
var roleMode = fullName.match(/^GROUP_/);
var currentUserMode = fullName == person.properties.userName;

var users = [];
if(roleMode) {
	users = deputies.getRoleDeputies(fullName);
} else if(currentUserMode) {
	users = deputies.getCurrentUserDeputies();
} else {
	users = deputies.getUserDeputies(fullName);
}

var allUsers = [];
for(var i in users) {
	var user = users[i];
	var available = availability.getUserAvailability(user.userName);
	allUsers.push({
		"user": user,
		"available": available,
		"deputy": true,
		"canDelete": currentUserMode
	});
}

model.deputies = allUsers;

})();
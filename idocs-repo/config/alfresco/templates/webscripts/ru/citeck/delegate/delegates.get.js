(function() {

var fullName = url.templateArgs.fullName;
var roleMode = fullName.match(/^GROUP_/);
var currentUserMode = fullName == person.properties.userName;

var users = [];
if(roleMode) {
	users = delegates.getRoleDelegates(fullName);
} else if(currentUserMode) {
	users = delegates.getCurrentUserDelegates();
} else {
	users = delegates.getUserDelegates(fullName);
}

var allUsers = [];
for(var i in users) {
	var user = users[i];
	var available = availability.getUserAvailability(user.userName);
	allUsers.push({
		"user": user,
		"available": available,
		"delegate": true,
		"canDelete": currentUserMode
	});
}

model.delegates = allUsers;

})();
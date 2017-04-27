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

var delegated = false;
if(roleMode) {
	delegated = delegates.isRoleDelegatedToUser(fullName, userName);
} else {
	var users = [];
	if(currentUserMode) {
		users = delegates.getCurrentUserDelegates();
	} else {
		users = delegates.getUserDelegates(fullName);
	}
	for(var i in users) {
		if(users[i].userName == userName) {
			delegated = true;
			break;
		}
	}
}

if(!delegated) {
	status.setCode(status.STATUS_NOT_FOUND, "User " + userName + " is not delegate of " + fullName);
	return;
}

}

model.delegate = {
	"user": user,
	"available": availability.getUserAvailability(userName),
	"canDelete": true,
	"delegate": true
};

})();
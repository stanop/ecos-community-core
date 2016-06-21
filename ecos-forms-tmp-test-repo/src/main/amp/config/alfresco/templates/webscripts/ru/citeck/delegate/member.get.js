(function() {

var roleName = url.templateArgs.roleName;
var userName = url.templateArgs.userName;

var user = groups.getUser(userName);
if(!user) {
	status.setCode(status.STATUS_NOT_FOUND, "No such user: " + userName);
	return;
}

var delegated = delegates.isRoleDelegatedToUser(roleName, user.userName);

// we should check, that the user is role member
if(!delegated) {
	var roles = delegates.getUserRoles(userName);
	var isMember = false;
	for(var i in roles) {
		if(roles[i].fullName == roleName) {
			isMember = true;
			break;
		}
	}
	if(!isMember) {
		status.setCode(status.STATUS_NOT_FOUND, "User " + userName + " is neither member, nor delegate for role " + roleName);
		return;
	}
}

var managed = delegates.isRoleDelegatedByUser(roleName, user.userName);
var available = availability.getUserAvailability(user.userName);

model.member = {
	"user": user,
	"manage": managed,
	"delegate": delegated,
	"available": available
};

})();
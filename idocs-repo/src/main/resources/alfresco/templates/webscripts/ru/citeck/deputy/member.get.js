(function() {

var roleName = url.templateArgs.roleName;
var userName = url.templateArgs.userName;

var user = groups.getUser(userName);
if(!user) {
	status.setCode(status.STATUS_NOT_FOUND, "No such user: " + userName);
	return;
}

var deputied = deputies.isRoleDeputiedToUser(roleName, user.userName);

// we should check, that the user is role member
if(!deputied) {
	var roles = deputies.getUserRoles(userName);
	var isMember = false;
	for(var i in roles) {
		if(roles[i].fullName == roleName) {
			isMember = true;
			break;
		}
	}
	if(!isMember) {
		status.setCode(status.STATUS_NOT_FOUND, "User " + userName + " is neither member, nor deputy for role " + roleName);
		return;
	}
}

var managed = deputies.isRoleDeputiedByUser(roleName, user.userName);
var available = availability.getUserAvailability(user.userName);

model.member = {
	"user": user,
	"manage": managed,
	"deputy": deputied,
	"available": available
};

})();
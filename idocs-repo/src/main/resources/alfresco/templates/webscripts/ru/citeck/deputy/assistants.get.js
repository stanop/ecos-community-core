(function () {

    var fullName = url.templateArgs.fullName;
    var roleMode = fullName.match(/^GROUP_/);
    var currentUserMode = fullName == person.properties.userName;

    var users = [];
    if (roleMode) {
        users = deputies.getRoleAssistants(fullName);
    } else if (currentUserMode) {
        users = deputies.getCurrentUserAssistants();
    } else {
        users = deputies.getUserAssistants(fullName);
    }

    var allUsers = [];
    for (var i in users) {
        var user = users[i];
        var available = availability.getUserAvailability(user.userName);
        logger.log("fullName = " + fullName + ", userName = " + user.userName);
        allUsers.push({
            "user": user,
            "available": available,
            "deputy": true,
            "canDelete": currentUserMode,
            "isAssistant": true
        });
    }

    model.assistants = allUsers;

})();
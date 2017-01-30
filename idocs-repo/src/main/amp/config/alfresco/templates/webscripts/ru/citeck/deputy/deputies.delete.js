(function () {

    var fullName = url.templateArgs.fullName;
    var roleMode = fullName.match(/^GROUP_/);
    var currentUserMode = fullName == person.properties.userName;

    if (args.users == null || args.users == "") {
        status.setCode(status.STATUS_BAD_ARGUMENT, "users argument is mandatory");
        return;
    }

    var users = args.users.split(/,/);
    var isAssistants = args.isAssistants == 'true';
    if (isAssistants) {
        if (roleMode) {
            deputies.removeRoleAssistants(fullName, users);
        } else if (currentUserMode) {
            deputies.removeCurrentUserAssistants(users);
        } else {
            users = deputies.removeUserAssistants(fullName, users);
        }
        model.success = true;
        return;
    }

    if (roleMode) {
        deputies.removeRoleDeputies(fullName, users);
    } else if (currentUserMode) {
        deputies.removeCurrentUserDeputies(users);
    } else {
        users = deputies.removeUserDeputies(fullName, users);
    }

    model.success = true;

})();
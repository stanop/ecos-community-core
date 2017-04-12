(function () {

    var fullName = url.templateArgs.fullName;
    var roleMode = fullName.match(/^GROUP_/);
    var currentUserMode = fullName == person.properties.userName;
    var allUsers = [];

    function getDeputies() {
        var users = [];


        if (roleMode) {
            users = deputies.getRoleDeputies(fullName);
        } else if (currentUserMode) {
            users = deputies.getCurrentUserDeputies();
        } else {
            users = deputies.getUserDeputies(fullName);
        }


        for (var i in users) {
            var user = users[i];
            var available = availability.getUserAvailability(user.userName);
            allUsers.push({
                "user": user,
                "available": available,
                "deputy": true,
                "canDelete": currentUserMode,
                "isAssistant": false
            });
        }
    }

    function getAssistants() {
        var users = [];

        if (roleMode) {
            users = deputies.getRoleDeputies(fullName);
        } else if (currentUserMode) {
            users = deputies.getCurrentUserAssistants();
        } else {
            users = deputies.getUserAssistants(fullName);
        }


        for (var i in users) {
            var user = users[i];
            var available = availability.getUserAvailability(user.userName);

            allUsers.push({
                "user": user,
                "available": available,
                "deputy": true,
                "canDelete": currentUserMode,
                "isAssistant": true
            });
        }
    }

    getDeputies();
    getAssistants();

    model.deputies = allUsers;

})();


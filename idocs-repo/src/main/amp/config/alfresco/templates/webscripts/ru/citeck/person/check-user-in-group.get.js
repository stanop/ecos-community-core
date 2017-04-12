(function() {

    var userName = args.userName,
        groupName = args.groupFullName,
        exists = false;

    if (userName && groupName) {
        exists = checkUserInGroup(userName, groupName);
    } else {
        status.setCode(status.STATUS_BAD_REQUEST, "Either user or group should be specified");
        return; 
    }

    function checkUserInGroup(userName, groupName) {
        var person = people.getPerson(userName);
        var personGroups = people.getContainerGroups(person);
        var checkedGroup = people.getGroup(groupName);
        
        for (var i in personGroups) {
            if (!personGroups.hasOwnProperty(i))
                continue;
            for (var j = 0; j < arguments.length; j++) {
                var group = people.getGroup(arguments[j]);
                if (group) {
                    if (personGroups[i].equals(checkedGroup)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    model.data = exists.toString();
    
})();
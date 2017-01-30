var groupMember = false;
var userName = args.userName;
var groupName = args.groupName;
if (userName && groupName) {
    try {
        var user_group = people.getGroup(groupName);
        if (user_group) {
            var members = people.getMembers(user_group);
            if (members) {
                for (var i in members) {
                    var member = members[i];
                    if (member) {
                        if (member.properties['cm:userName'] == person.properties.userName) {
                            groupMember = true;
                            break;
                        }
                    }
                }
            }
        }
    } catch (e) {
        logger.log(e);
        throw "Can't check groups " + groupName + " on user " + userName;
    }
}
model.data = groupMember.toString();


<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

    var node = search.findNode(args['nodeRef']);
    var sPermissions = node.getFullPermissions(), permissions = [];

    for (var i in sPermissions) {
        var tokens = sPermissions[i].split(";");
        var access = tokens[0], authorityId = tokens[1], permission = tokens[2], inherit = tokens[3];
        var authority = {};
        var briefPropertyAlias = '';

        if (authorityId.indexOf('GROUP_') === 0) {
            authority = Common.getGroup(authorityId);
            briefPropertyAlias = 'shortName';
        } else if (authorityId.indexOf('ROLE_') === 0) {
            authority = {
                avatar: null,
                name: authorityId
            };
            briefPropertyAlias = 'name';
        } else {
            authority = Common.getPerson(authorityId);
            briefPropertyAlias = 'displayName';
        }

        permissions.push({
            'access': access,
            'authority': {
                avatar: authority['avatar']|| null,
                name: authorityId,
                displayName: authority[briefPropertyAlias]
            },
            'permission': permission,
            'inherit': 'INHERITED' == inherit
        });
    }

    model.data = {
        'inherit': node.inheritsPermissions(),
        'permissions': permissions
    }
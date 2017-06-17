<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">
/*
{
    "permissions": [
        "REMOVE;user3;All",
        "REMOVE;user2;All",
        "ADD;user4;Coordinator",
        "ADD;GROUP_usergroup1;Consumer"
    ],
    "inherit": false
}
*/
    var node = search.findNode(args['nodeRef']);
    var json = eval('(' + args['json'] + ')');
    var permissions = json['permissions'];

    var i = 0; //current permission index

    function rollback(i) {
        i -= 1;
        for (; i >= 0 ; i--) {
            var work = [];
            work = String(permissions.get(i)).split(";");
            switch (work[0].toLowerCase()) {
                default: break;
                case 'add':
                    node.removePermission(work[2], work[1]);
                    break;
                case 'remove':
                    node.setPermission(work[2], work[1]);
                    break;
            }
        }
    }


    function validateAuthority(authority) {
    }

    for (i = 0; i < permissions.length; i++) {
        var work = permissions[i].split(";");

            switch (work[0].toLowerCase()) {
                default: throw "UnknownActionException";
                case "add":
                    validateAuthority(work[1]);
                    permissionService.setPermission(
                        args['nodeRef'],
                        work[2], //permission
                        work[1], //authority
                        work[3]  //allow
                    );
                    break;
                case "remove":
                    validateAuthority(work[1]);
                    permissionService.deletePermission(
                        args['nodeRef'],
                        work[2], //permission
                        work[1]  //authority
                    );
                    break;
                case "clear_authority":
                    validateAuthority(work[1]);
                    permissionService.clearPermissions(
                        args['nodeRef'],
                        work[1] //authority
                    );
                    break;
                case "clear_store":
                    permissionService.deletePermissions(
                        args['store_type'],
                        args['store_id']
                    );
                    break;
                case "set_inherit_permissions":
                    permissionService.setInheritParentPermissions(
                        args['nodeRef'],
                        work[1] //inherit:boolean
                    );
                    break;
            }
    }


    function returnResult(node) {
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
    }

    returnResult(node);
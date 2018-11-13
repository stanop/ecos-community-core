(function() {
    var groupFullName = args['groupFullName'],
        authorityScriptGroup = groups.getGroupForFullAuthorityName(groupFullName),
        authorityNodeRef = authorityScriptGroup ? authorityScriptGroup.groupNodeRef.toString() : '';
    model.nodeRef = authorityNodeRef;
})();

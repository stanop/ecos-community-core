<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = (function() {
	function isNodeRef(str) {
		return str && (str.indexOf('workspace://') == 0 || str.indexOf('alfresco://') == 0 || str.indexOf('archive://') == 0);
	}
	var groupName = '_orgstruct_home_';
	var group = groups.getGroup(groupName);
	if(group == null) {
		status.setCode(status.STATUS_NOT_FOUND, "Group " + groupName + " is not found");
		return;
	}

	var nodeRef = args.nodeRef,
		options = getFilterOptions();

	var caseNode = null;
	if (isNodeRef(nodeRef)) {
		caseNode = search.findNode(nodeRef);
	}
	else {
		if (isNodeRef(args.protocol)) {
			var protocol = search.findNode(args.protocol);
			if (protocol)
				caseNode = protocol.parent;
		}
	}
	if (!caseNode) {
		status.setCode(status.STATUS_NOT_FOUND, "Case node is not found by nodeRef=" + nodeRef);
		return;
	}
	var agendas = caseNode.childAssocs['meet:childAgenda'];
	if (!agendas || !agendas[0]) {
		status.setCode(status.STATUS_NOT_FOUND, "Agenda is not found in the case nodeRef=" + nodeRef);
		return;
	}
	var agenda = agendas[0];
	var participants = agenda.assocs['meet:plannedParticipants'];
	var result = [];
	if (participants && participants.length)
	{
		for each(var u in group.allUsers) {
			for each(var p in participants) {
				if (p.nodeRef && u.person && p.nodeRef.equals(u.person.nodeRef)) {
					result.push(u);
					break;
				}
			}
		}
	}
	return [].concat(result);
})();

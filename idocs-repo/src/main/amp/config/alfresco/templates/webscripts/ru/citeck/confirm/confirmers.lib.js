/**
 * It checks mandatory input arguments and
 * current user belonging to GROUP_skipMandatoryConfirm.
 * @returns (boolean) It returns false if mandatory arguments is absent or
 * current user belong to GROUP_skipMandatoryConfirm
 */
function check() {
	if (!search.findNode(args.nodeRef)) {
		status.setCode(status.STATUS_NOT_FOUND, "Could not find node by mandatory nodeRef=" + args.nodeRef);
		return false;
	}

	var result = true;
	var personAuthority = groups.getGroupForFullAuthorityName(person.properties.userName);
	var personGroups = personAuthority.allParentGroups;
	for(var i in personGroups) {
		if (personGroups[i].fullName == "GROUP_skipMandatoryConfirm") {
			result = false;
			model.precedence = { "stages": [] };
			break;
		}
	}
	return result;
}

/**
 * It returns branch manager.
 * @param branch - (object) from orgstruct
 * @returns if manager exists in specified branch, it returns him,
 * 		in other case it returns null.
 */
function getBranchManager(branch) {
	if (!branch.childGroups)
		return null;
	var childGroups = branch.childGroups;
	// check every group
	for (var i in childGroups) {
		var childName = childGroups[i].fullName;
		// should be role
		if (!orgstruct.isTypedGroup("role", childName))
			continue;
		// should be manager role
		var roleTypeName = orgstruct.getGroupSubtype(childName);
		var roleType = orgmeta.getSubType("role", roleTypeName);
		if (roleType.properties["org:roleIsManager"] != true)
			continue;
		return childGroups[i];
	}
	return null;
}

/**
 * It returns a first branch manger by specified authority.
 * @param branch - (object) from orgstruct
 * @returns if manager exists in specified branch or its parents, it returns him,
 * 		in other case it returns null.
 */
function getFirstBranchManagerRecursive(branch) {
	if (branch == null)
		return null;

	var manager = getBranchManager(branch);
	if (manager == null) {
		if (branch.parentGroups) {
			for ( var i in branch.parentGroups) {
				if (!branch.parentGroups.hasOwnProperty(i))
					continue;
				manager = getFirstBranchManagerRecursive(branch.parentGroups[i]);
				if (manager != null)
					break;
			}
		}
	}
	return manager;
}

/**
 * It converts branch object to stage object.
 * @param authorities - (array) array of authorities from orgstruct or array
 * of group full names.
 */
function getStage(authorities) {
	var result = {
			dueDate: null,
			confirmers: []
	};
	if (authorities) {
		if (typeof authorities !== 'object' || !authorities.hasOwnProperty)
			authorities = [authorities];
		for (var i in authorities) {
			if (!authorities.hasOwnProperty(i))
				continue;
			if (typeof authorities[i] === 'string') {
				if (logger.loggingEnabled)
					logger.log('Trying to get authority for ' + authorities[i]);
				authorities[i] = groups.getGroupForFullAuthorityName(authorities[i]);
			}
			if (typeof authorities[i] === 'object' && authorities[i] !== null) {
				result.confirmers.push({
					nodeRef: '' + authorities[i].groupNodeRef,
					fullName: '' + authorities[i].fullName,
					canCancel: false
				});
			}
		}
	}
	return result;
}

/**
 * It converts branch object to stage object.
 * @param authorities - (array) array of authorities from orgstruct or array
 * of group full names.
 */
function checkBelongsUserToGroup(group) {
	var result = false;
	var personAuthority = groups.getGroupForFullAuthorityName(person.properties.userName);
	var personGroups = personAuthority.allParentGroups;
	var result = false;
	for(var i in personGroups) {
		if (personGroups[i].fullName == group) {
			result = true;
			break;
		}
	}
	return result;
}

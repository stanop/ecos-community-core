<import resource="classpath:alfresco/templates/webscripts/ru/citeck/confirm/confirmers.lib.js">

(function() {

	if (!check())
		return;

	var personAuthority = groups.getGroupForFullAuthorityName(person.properties.userName),
		manager = getFirstBranchManagerRecursive(personAuthority),
		stages = getStage([manager]);

	logger.log("Idocs. Collected " + stages.length + " stages.");

	model.precedence = {
		"stages": stages
	};

})();

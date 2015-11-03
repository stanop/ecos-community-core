/**
 * This file helps to get destination node reference by input parameter args.destination.
 * 
 * If args.destination is URL to web-script which returns node reference in
 * JSON and args.destinationResult is a name of member in JSON to destination
 * node reference, so this function sets model.destination to gotten node
 * reference.
 * 
 * If args.destination is node reference, this function checks permissions
 * (by default - CreateChildren) or by permissions defined in
 * args.destinationPermissions (divided by comma). The result of checking
 * permissions is stored in variable: model.destinationPermissionsResult
 * 
 * Before importing this file, firstly should be included:
 * <import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
 */
(function() {
	function isNodeRef(str) {
		return str && (str.indexOf('workspace://') == 0 || str.indexOf('alfresco://') == 0 || str.indexOf('archive://') == 0);
	}

	function getConfigResult(json, config) {
		var result = json;
		if (config) {
			var fields = config.split(/[.]/);
			for(var i in fields) {
				if(!fields.hasOwnProperty(i))
					continue;
				if(!result)
					break;
				result = result[fields[i]];
			}
		}
		if (result == json)
			result = null;
		return result;
	}

	function getNodeRefs(arr) {
		var result = [];
		if (arr && arr.length) {
			for (var i = 0; i < arr.length; i++) {
				if (arr[i].nodeRef)
					result.push(arr[i].nodeRef);
			}
		}
		return result;
	}

	var destination = AlfrescoUtil.param('destination', null);
	var destinationResult = AlfrescoUtil.param('destinationResult', null);
	var destinationPermissions = AlfrescoUtil.param('destinationPermissions', null);
	var calcDestinationChildrenCount = AlfrescoUtil.param('calcDestinationChildrenCount', null);

	if (destination && !isNodeRef(destination)) {
		var connector = remote.connect("alfresco");
		var result = connector.get(destination);
		if (result.status != 200) {
			AlfrescoUtil.error(result.status, 'Can not get destination nodeRef: request URL = ' + destination);
			return;
		}
		var json = result != "" ? eval('(' + result + ')') : "";
		destinationResult = destinationResult ? destinationResult : 'nodeRef';
		destination = getConfigResult(json, destinationResult);
		model.destination = destination;
		model.destinationPermissionsResult = isNodeRef(model.destination);
	}
	if (destination && isNodeRef(model.destination)) {
		AlfrescoUtil.param('site', null);
		var d = AlfrescoUtil.getNodeDetails(model.destination, model.site);
		destinationPermissions = destinationPermissions ? destinationPermissions.split(',') : ["CreateChildren"];
		if (d && d.item && d.item.node && d.item.node.permissions && d.item.node.permissions.user) {
			var result = true;
			for (var i = 0; i < destinationPermissions.length; i++) {
				result = result && (d.item.node.permissions.user[destinationPermissions[i]] || false);
			}
			model.destinationPermissionsResult = result;
		}
	}
	if (destination && calcDestinationChildrenCount === 'true') {
		var count = 0;
		model.destinationChildrenNodeRefs = [];
		AlfrescoUtil.param('nodeRef', null);
		AlfrescoUtil.param('contentType', null);
		AlfrescoUtil.param('assocType', null);
		if (model.nodeRef && model.contentType && model.assocType) {
			var connector = remote.connect("alfresco");
			var url = '/citeck/assocs?nodeRef=' + model.nodeRef + '&assocTypes=' + model.assocType + '&contentTypes=' + model.contentType + '&addAssocs=false';
			var result = connector.get(url);
			if (result.status != 200) {
				AlfrescoUtil.error(result.status, 'Request is failed. URL = ' + url);
				return;
			}
			var json = result != "" ? eval('(' + result + ')') : "";
			if (json && json.sources) {
				var out = getNodeRefs(json.sources);
				if (out.length) {
					count += out.length;
					model.destinationChildrenNodeRefs = model.destinationChildrenNodeRefs.concat(out);
				}
			}
			if (json && json.targets) {
				var out = getNodeRefs(json.targets);
				if (out.length) {
					count += out.length;
					model.destinationChildrenNodeRefs = model.destinationChildrenNodeRefs.concat(out);
				}
			}
			if (json && json.children) {
				var out = getNodeRefs(json.children);
				if (out.length) {
					count += out.length;
					model.destinationChildrenNodeRefs = model.destinationChildrenNodeRefs.concat(out);
				}
			}
		}
		model.destinationChildrenCount = count;
	}
})();

function main() {
	try {
		var filename = null,
			content = null,
			destination = null,
			destNode = null,
			assocType = null,
			propertyName = null,
			propertyValue = null,
			properties = {};

		var contentType = null,
			overwrite = true;

		// ------------------------
		// Getting input parameters
		// ------------------------

		if (args["lang"]) utils.setLocale("" + args["lang"]);
		if (args["assoctype"]) assocType = "" + args["assoctype"];
		if (args["contenttype"]) contentType = "" + args["contenttype"];
		if (args["propertyname"]) propertyName = "" + args["propertyname"];
		if (args["propertyvalue"]) propertyValue = "" + args["propertyvalue"];
		var assoc = getAssoc(assocType);

		// Parse file attributes
		for each (field in formdata.fields) {
			var fieldName = String(field.name).toLowerCase();

			if (fieldName.indexOf("property_") != -1) {
				properties[fieldName.replace("property_", "")] = field.value;
				continue;
			}

			switch (fieldName) {
				case "filedata":
					if (field.isFile) {
						filename = String(field.filename);
						content = field.content;
					}
					break;
				case "destination":
					destination = "" + field.value;
					destNode = search.findNode(destination);
					break;
				case "overwrite":
					overwrite = ("" + field.value) == "true";
					break;
			}		
		}

		// ------------------------
		// Checking input parameters
		// ------------------------

		if (!filename || !content || !destination || !assocType) {
			exitUpload(400, "Required parameters are missing");
			return;
		}
		
		if (!destNode) {
			exitUpload(404, "Destination (" + destination + ") not found");
			return;
		}

		if (!assoc) {
			exitUpload(404, "Specified association type (" + assocType + ") is not found");
			return;
		}

		if (!assoc.isChild()) {
			exitUpload(400, "Specified association type (" + assocType + ") is not a child association");
			return;
		}

		// ------------------------
		// Uploading
		// ------------------------

		contentType = !contentType ? getContentType(assocType) : contentType;
		var existingFile;

		model.properties = properties;
		
		if(!assoc.isTargetMany()) {
			existingFile = destNode.childAssocs[assocType] && destNode.childAssocs[assocType][0];
			if (existingFile) { model.document = uploadVersionableFile(filename, content, existingFile, properties); }
			else { model.document = uploadFile(filename, contentType, assocType, content, destNode, properties); }
		} else {
			existingFile = childByFileName(destNode, filename, assocType);
			if (existingFile && !assoc.getDuplicateChildNamesAllowed()) {
				if (overwrite) {
					existingFile.ensureVersioningEnabled(true, false); 
					model.document = uploadVersionableFile(filename, content, existingFile, properties);
				} else {
					filename = getUniqueFileName(destNode, filename, existingFile, assocType);
					model.document = uploadFile(filename, contentType, assocType, content, destNode, properties);
				}
			} else {
				model.document = uploadFile(filename, contentType, assocType, content, destNode, properties);
			}
		}

		setProperty(model.document, propertyName, propertyValue);

		formdata.cleanup();
	} catch (e) {
		if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0) { e.code = 413; } 
		else { e.code = 500; }
		throw e;
	}
}

main();


// ---------
// FUNCTIONS
// ---------

function childByFileName(destNode, filename, assocType) {
	var result = null,
	 	nodeService = services.get("NodeService"),
	 	qn = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(assocType)),
	 	childRef = nodeService.getChildByName(destNode.getNodeRef(), qn, filename);

	if (childRef) result = search.findNode(childRef.toString());
	return result;
}

function extractMetadata(file) {
	var emAction = actions.create("extract-metadata");
	if (emAction) { emAction.execute(file, false, false); }
}

function uploadFile(filename, contentType, assocType, content, destNode, properties) {
	var node = destNode.createNode(filename, contentType, null, assocType);

	for (var p in properties) { node.properties[p] = properties[p]; }

	node.properties["cm:name"] = filename;
	node.properties.content.write(content, false, true);
	node.properties.content.guessMimetype(filename);
	node.save();

	extractMetadata(node);
	return node;
}

function uploadVersionableFile(filename, content, node, properties) {
	node.properties.content.write(content);

	for (var p in properties) { node.properties[p] = properties[p]; }

	node.properties.content.guessMimetype(filename);
	node.properties.content.guessEncoding();
	node.save();

	extractMetadata(node);
	return node;
}

function getUniqueFileName(destNode, filename, fileNode, assocType) {
	var counter = 1,
		result = filename,
		dotIndex = filename.lastIndexOf(".");

	var startName = filename.substring(0, dotIndex),
		extensionName = filename.substring(dotIndex);

	if (fileNode == null)
		fileNode = childByFileName(destNode, filename, assocType);

	while (fileNode != null) {
		if (dotIndex == 0) { result = counter + filename; } 
		else if (dotIndex > 0) { result = startName + "-" + counter + extensionName; } 
		else { result = filename + "-" + counter; }

		fileNode = childByFileName(destNode, result, assocType);
		counter++;
	}
	return result;
}

function getAssoc(assocType) {
	var dictionaryService = services.get("DictionaryService");
	var qn = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(assocType));
	return dictionaryService.getAssociation(qn);
}

function getContentType(assocType) {
	var result = null;
	var cmContent = "cm:content";
	var dictionaryService = services.get("DictionaryService");
	if (dictionaryService != null) {
		var contentQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(cmContent));
		var assoc = getAssoc(assocType);
		if (assoc != null) {
			var tc = assoc.getTargetClass().getName();
			if (dictionaryService.isSubClass(tc, contentQName))
				result = tc.toString();
			else if (dictionaryService.isSubClass(contentQName, tc))
				result = contentQName.toString();
		}
	}
	return result;
}

function setProperty(node, name, value) {
	if (name && value) {
		var props = node.properties;
		props[name] = value;
		node.save();
	}
}

function exitUpload(statusCode, statusMsg) {
	status.code = statusCode;
	status.message = statusMsg;
	status.redirect = true;
	formdata.cleanup();
}
function childByFileName(destNode, filename, assocType) {
	var result = null;
	var nodeService = services.get("NodeService");
	var qn = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(assocType));
	var childRef = nodeService.getChildByName(destNode.getNodeRef(), qn, filename);
	if (childRef !== null)
		result = search.findNode(childRef.toString());
	return result;
}

function extractMetadata(file) {
	// Extract metadata - via repository action for now.
	// This should use the MetadataExtracter API to fetch properties, allowing
	// for possible failures.
	var emAction = actions.create("extract-metadata");
	if (emAction !== null) {
		// Call using readOnly = false, newTransaction = false
		emAction.execute(file, false, false);
	}
}

function uploadFile(filename, contentType, assocType, content, destNode) {
	// Create new node
	var node = destNode.createNode(filename, contentType, null, assocType);

	// Use a the appropriate write() method so that the mimetype already
	// guessed from the original filename is
	// maintained - as upload may have been via Flash - which always sends
	// binary mimetype and would overwrite it.
	// Also perform the encoding guess step in the write() method to save an
	// additional Writer operation.
	node.properties["cm:name"] = filename;
	node.properties.content.write(content, false, true);

	if (logger.debugLoggingEnabled) {
		logger.debug(content);
	}

	node.properties.content.guessMimetype(filename);
	node.save();

	// Extract the metadata
	extractMetadata(node);
	return node;
}

function uploadVersionableFile(filename, content, node) {
	// Upload component was configured to overwrite files if name clashes
	node.properties.content.write(content);

	// Reapply mimetype as upload may have been via Flash - which
	// always sends binary mimetype
	node.properties.content.guessMimetype(filename);
	node.properties.content.guessEncoding();
	node.save();

	// Extract the metadata
	// (The overwrite policy controls which if any parts of
	// the document's properties are updated from this)
	extractMetadata(node);
	return node;
}

function getUniqueFileName(destNode, filename, fileNode, assocType) {
	// Upload component was configured to find a new unique name for
	// clashing filenames
	var counter = 1,
		result = filename,
		dotIndex = filename.lastIndexOf(".");

	var startName = filename.substring(0, dotIndex),
		extensionName = filename.substring(dotIndex);

	if (fileNode === null)
		fileNode = childByFileName(destNode, filename, assocType);

	while (fileNode !== null) {
		if (dotIndex === 0) {
			// File didn't have a proper 'name' instead it had just
			// a suffix and started with a ".", create "1.txt"
			result = counter + filename;
		} else if (dotIndex > 0) {
			// Filename contained ".", create "filename-1.txt"
			result = startName + "-" + counter + extensionName;
		} else {
			// Filename didn't contain a dot at all, create
			// "filename-1"
			result = filename + "-" + counter;
		}
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
	if (dictionaryService !== null) {
		var contentQName = Packages.org.alfresco.service.namespace.QName.createQName(utils.longQName(cmContent));
		var assoc = getAssoc(assocType);
		if (assoc !== null) {
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

function exitUpload(statusCode, statusMsg, debugMsg) {
	status.code = statusCode;
	status.message = statusMsg + (debugMsg ? ". Debug: " + debugMsg : "");
	status.redirect = true;
	formdata.cleanup();
}

function main() {
	try {
		var filename 			= null,
				content 			= null,
				destination 	= null,
				destNode 			= null,
				assocType 		= null,
				propertyName 	= null,
				propertyValue = null;

		// Upload specific
		var contentType = null,
				overwrite   = true; // If a filename clashes for a versionable file

		// ------------------------
		// Getting input parameters
		// ------------------------

		if (args["lang"] !== null)
			utils.setLocale("" + args["lang"]);
		if (args["assoctype"] !== null)
			assocType = "" + args["assoctype"];
		if (args["contenttype"] !== null)
			contentType = "" + args["contenttype"];
		if (args["propertyname"] !== null)
			propertyName = "" + args["propertyname"];
		if (args["propertyvalue"] !== null)
			propertyValue = "" + args["propertyvalue"];

		var assoc = getAssoc(assocType);

		// Parse file attributes
		for (var f in formdata.fields) {
			var field = formdata.fields[f],
					fieldName = field.name.toString().toLowerCase();

  		if (fieldName == "filedata") {
				if (field.isFile) {
					filename = field.filename.toString();
					content = field.content;
				}
			} else if (fieldName == "destination") {
				destination = field.value.toString().replace("\\", "");
				destNode = search.findNode(destination);
			} else if (fieldName == "overwrite") {
				overwrite = field.value.toString() == "true";
			}
		}

		// ------------------------
		// Checking input parameters
		// ------------------------

		// Ensure mandatory file attributes have been located. Also need destination
		if (filename === null || content === null || destination === null || assocType === null) {
			exitUpload(400, "Required parameters are missing");
			return;
		}

		if (destNode === null) {
			exitUpload(404, "Destination (" + destination + ") not found");
			return;
		}

		if (assoc === null) {
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

		contentType = contentType === null ? getContentType(assocType) : contentType;
		var existingFile;

		// First of all we are checking the target association type
		if(!assoc.isTargetMany()) {
			existingFile = destNode.childAssocs[assocType] && destNode.childAssocs[assocType][0];
			if (existingFile !== null) {
				model.document = uploadVersionableFile(filename, content, existingFile);
			} else { model.document = uploadFile(filename, contentType, assocType, content, destNode); }
		} else {
			existingFile = childByFileName(destNode, filename, assocType);
			if (existingFile !== null && !assoc.getDuplicateChildNamesAllowed()) {
				if (overwrite) {
					existingFile.ensureVersioningEnabled(true, false);
					model.document = uploadVersionableFile(filename, content, existingFile);
				} else {
					filename = getUniqueFileName(destNode, filename, existingFile, assocType);
					model.document = uploadFile(filename, contentType, assocType, content, destNode);
				}
			} else {
				model.document = uploadFile(filename, contentType, assocType, content, destNode);
			}
		}

		setProperty(model.document, propertyName, propertyValue);

		// It is IMPORTANT final operation.
		formdata.cleanup();
	}	catch (e) {
		// NOTE: Do not clean formdata temp files to allow for retries. It's
		// possible for a temp file
		// to remain if max retry attempts are made, but this is rare, so leave
		// to usual temp
		// file cleanup.

		// capture exception, annotate it accordingly and re-throw
		if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") === 0) {
			e.code = 413;
		} else {
			e.code = 500;
		  e.message = "Unexpected error occurred during upload of new content.";
		}

		throw e;
	}
}

main();

function buildDocumentModel(document) {
    return {
        node: document,
        uploaded: document.properties['cm:modified'],
        uploader: people.getPerson(document.properties['cm:modifier'])
    };
}

function exitUpload(statusCode, statusMsg) {
    status.code = statusCode;
    status.message = statusMsg;
    status.redirect = true;
    formdata.cleanup();
}

function extractMetadata(file) {
    var emAction = actions.create("extract-metadata");
    if (emAction != null) {
        // Call using readOnly = false, newTransaction = false
        emAction.execute(file, false, false);
    }
}

function uniqFilename(filename, container) {
    var existingDocument = container.childByNamePath(filename),
        index = 1,
        extensionRegexp = /^(.+)\.(\w+)$/,
        originalFilename = filename;

    while(existingDocument != null) {
        var fileWithExtension = originalFilename.match(extensionRegexp);
        if(fileWithExtension) {
            filename = fileWithExtension[1] + " (" + index + ")." + fileWithExtension[2];
        } else {
            filename = originalFilename + " (" + index + ")";
        }

        index++;
        existingDocument = container.childByNamePath(filename);
    }

    return filename;
}

(function() {
    
    var containerNodeRef = null, container = null,
        type = null, kind = null,
        multiple = false,
        document, docs = [],
        createVersion = false,
        files = [];
    
    for (var i in formdata.fields){
        var field = formdata.fields[i],
            fieldname = String(field.name).toLowerCase();

        if (fieldname.indexOf("filedata") != -1) {
            if (field.isFile) files.push(field);
        }

        switch (fieldname) {
            case "container":
                containerNodeRef = "" + field.value || null;
                break;
            case "type":
                type = "" + field.value || null;
                break;
            case "kind":
                kind = "" + field.value || null;
                break;
            case "multiple":
                multiple = field.value != "false";
                break;
        }
    }
    
    if(containerNodeRef) {
        container = search.findNode(containerNodeRef);
    } else {
        exitUpload(status.STATUS_BAD_REQUEST, "Argument 'container' should be specified");
        return;
    }
    
    if(!container) {
        exitUpload(status.STATUS_NOT_FOUND, "Can not find node " + container);
        return;
    }

    if(files.length == 0) {
        exitUpload(status.STATUS_BAD_REQUEST, "Files is not specified");
        return;
    }

    if(type != null && kind != null && multiple == false) {
        // search existing document with specified type & kind
        var documents = container.children;
        for(var i in documents) {
            if(documents[i].properties['tk:type'] && documents[i].properties['tk:type'].nodeRef == type 
            && documents[i].properties['tk:kind'] && documents[i].properties['tk:kind'].nodeRef == kind) {
                document = documents[i];
                createVersion = true;
                break;
            }
        }
    }


    // create categories, otherwise type & kind are not saved
    var typeRoot = search.findNode('workspace://SpacesStore/category-document-type-root'),
        typeNode = search.findNode(type),
        kindNode = search.findNode(kind);

    if(typeNode == null) {
        var id = type.replace('workspace://SpacesStore/', '');
        typeNode = typeRoot.createNode(null, 'cm:category', {
            'sys:node-uuid': id,
            'cm:name': id
        }, 'cm:subcategories');
    }

    if(kindNode == null) {
        var id = kind.replace('workspace://SpacesStore/', '');
        kindNode = typeRoot.createNode(null, 'cm:category', {
            'sys:node-uuid': id,
            'cm:name': id
        }, 'cm:subcategories');
    }


    if (createVersion) {
        if (files.length > 1) 
            files = [files[files.length - 1]];
    }

    // create documents
    for (var f in files) {
        var filename = uniqFilename(files[f].filename, container),
            content = files[f].content,
            doc = document || container.createNode(null, 'cm:content', { 'cm:name': filename }, "icase:documents");

        doc.properties['tk:type'] = new Packages.org.alfresco.service.cmr.repository.NodeRef(type);
        doc.properties['tk:kind'] = new Packages.org.alfresco.service.cmr.repository.NodeRef(kind);
        doc.save();
        
        if(createVersion && !doc.hasAspect("cm:workingcopy")) {
            doc.ensureVersioningEnabled(true, false); 
            doc = doc.checkoutForUpload();
        }

        if(createVersion) {
            doc = doc.checkin("", false);
        }

        doc.properties.content.write(content, false, true);
        doc.properties.content.guessMimetype(filename);
        doc.properties.content.guessEncoding();

        docs.push(doc);
    }
      
    // extractMetadata(document); // somehow it resets type and kind
    
    model.documents = [];
    for (var d in docs) {
        model.documents.push(buildDocumentModel(docs[d]));
    }
       
    formdata.cleanup();
    
})()
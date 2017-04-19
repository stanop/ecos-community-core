/**
 * @author Roman Makarskiy
 */
var status = "Complete";

var parser = {
    parserScriptName: "xml-to-node-parser.js",
    parserData: {
        uuidFromProp: "",
        uuidPrefix: "",
        cmNameFromProp: "",
        cmNamePrefix: "",
        cmTitleRuFromProp: "",
        cmTitleEnFromProp: "",
        titles: {
            en: "",
            ru: ""
        }
    },
    createNodes: function(xmlData) {
        var xmlDataNode = search.findNode(xmlData);

        if (!xmlDataNode || xmlDataNode.typeShort != "xni:data") {
            logger.warn(this.parserScriptName + ": failed find xml data - " + xmlData);
            return false;
        }

        var content = xmlDataNode.content;
        var xml = new XML(content);

        var path = xml.path;
        var type = xml.type;

        var root = this.helper.getRootNodeByPath(path);

        if (!root) {
            return false;
        }

        var startTime = new Date().getTime();
        this.parserData.uuidFromProp = xml.uuidFromProp;
        this.parserData.cmNameFromProp = xml.cmNameFromProp;
        this.parserData.cmNamePrefix = xml.cmNamePrefix;
        this.parserData.uuidPrefix = xml.uuidPrefix;
        this.parserData.cmTitleRuFromProp = xml.cmTitle_RU_fromProp;
        this.parserData.cmTitleEnFromProp = xml.cmTitle_EN_fromProp;

        var objects = this.helper.getObjects(xml);
        var objCount = objects.length;

        logger.warn(this.parserScriptName + " Start parse... type: " + type);
        logger.warn(this.parserScriptName + " Found " + objCount + " objects. Parse in progress...");

        batchExecuter.processArray({
            items: objects,
            batchSize: 200,
            threads: 4,
            onNode: function(row) {
                var propObj = parser.getProperties(row);

                if (parser.helper.uuidExists(propObj)) {
                    logger.warn(parser.parserScriptName + " Cannot create node, because node with uuid: '"
                        + propObj['sys:node-uuid'] +"' already exists.");
                    status = "Error";
                } else if (parser.helper.cmNameExists(path, propObj)) {
                    logger.warn(parser.parserScriptName + " Cannot create node, because node with cm:name - '"
                        + propObj['cm:name'] +"' already exists.");
                    status = "Error";
                } else {
                    var createdNode = root.createNode(null, type, propObj, "cm:contains");
                    parser.helper.fillNodeTitle(createdNode);
                    parser.helper.fillAssocs(row, createdNode);
                }
            }
        });

        setStatusAsync(xmlDataNode, status);
        var endTime = new Date().getTime();
        var executedTime = endTime - startTime;
        logger.warn(this.parserScriptName + " Parse ends in " + this.helper.millisToMinAndSeconds(executedTime)
            + " (min:sec)" + " (" + executedTime + " ms)");
    },
    getProperties: function (obj) {
        var propObj = {};

        var properties = obj.properties;
        var propCount = properties.*.length();

        var propValueForUuid = "";
        var propValueForCmName = "";


        for (var i = 0; i < propCount; i++) {
            var prop = properties.child(i);
            var propType = prop.name().toString().split('_').join(':');
            var propValue = prop.toString();

            if (propType != "cm:title:ru" && propType != "cm:title:en") {
                propObj[propType] = propValue;
            }

            if (!propValueForUuid) {
                propValueForUuid = this.helper.getUuidValue(propValue, prop);
            }

            if (!propValueForCmName) {
                propValueForCmName = this.helper.getCmNameValue(propValue, prop);
            }

            this.helper.fillTitleData(propValue, prop);
        }

        if (propValueForUuid) {
            propObj['sys:node-uuid'] = propValueForUuid.toString();
        }
        if (propValueForCmName) {
            propObj['cm:name'] = propValueForCmName.toString();
        }

        return propObj;
    },
    helper: {
        getObjects: function (xml) {
            var objects = [];
            for each (var i in xml.object) {
                objects.push(i);
            }
            return objects;
        },
        fillAssocs: function(obj, node) {
            var assocsData = obj.associations.association;

            if (assocsData.length() == 0) {
                return;
            }

            for each(var assocData in assocsData) {

                if (assocData.cm_authority.length() > 0) {
                    if (assocData.cm_authority.name.length() == 0 || assocData.assocType.length() == 0) {
                        logger.error(parser.parserScriptName
                            + " fillAssocs() - cm_authority: (name) or (assocType) parameter is empty. Method aborted.");
                        return;
                    }

                    var targetGroup = this.createGroup(assocData.cm_authority);
                    if (!targetGroup) {
                        logger.error(this.parserScriptName + " cannot create association with authority: "
                            + assocData.cm_authority.name);
                        return;
                    }

                    var groupNode = search.findNode(targetGroup.groupNodeRef);
                    node.createAssociation(groupNode, assocData.assocType);

                    if (assocData.cm_authority.parent_groups.group.length() > 0) {
                        var parentGroups = assocData.cm_authority.parent_groups.group;
                        for each (var parentGroup in parentGroups) {
                            var createdParentGroup = this.createGroup(parentGroup);
                            if (createdParentGroup && !this.childGroupExists(createdParentGroup, targetGroup.shortName)) {
                                createdParentGroup.addAuthority("GROUP_" + targetGroup.shortName);
                            }
                        }
                    }

                    if (assocData.cm_authority.child_groups.group.length() > 0) {
                        var childGroups = assocData.cm_authority.child_groups.group;
                        for each (var childGroup in childGroups) {
                            var createdChildGroup = this.createGroup(childGroup);
                            if (createdChildGroup && !this.childGroupExists(targetGroup, childGroup.name)) {
                                targetGroup.addAuthority("GROUP_" + childGroup.name);
                            }
                        }
                    }

                } else {
                    var targetNode = null;

                    if (assocData.uuid.length() > 0) {

                        if (assocData.assocType.length() == 0) {
                            logger.error(parser.parserScriptName + " cannot fill assocs from uuid: " + assocData.uuid
                                + ", because assocType is empty.");
                            return;
                        }

                        var uuid = assocData.uuid.toString().toLowerCase();
                        targetNode = search.findNode("workspace://SpacesStore/" + uuid);
                    } else {
                        if (assocData.assocType.length() == 0 || assocData.targetNodeType.length() == 0
                            || assocData.propId.length() == 0 || assocData.propValue.length() == 0
                            || assocData.targetNodePath.length() == 0) {
                            logger.error(parser.parserScriptName
                                + " cannot fill assocs from propId, because one of mandatory parameter is empty");
                            return;

                        }

                        var targetNodeRoot = this.getRootNodeByPath(assocData.targetNodePath);

                        if (!targetNodeRoot) {
                            return;
                        }

                        targetNode = this.getChildByProperty(targetNodeRoot, assocData.propId, assocData.propValue);
                    }

                    if (!targetNode.exists()) {
                        logger.error(this.parserScriptName + " targetNode does not exists. Information: " +
                            "\ntargetNodeRoot: " + targetNodeRoot.nodeRef + " prop id: "
                            + assocData.propId + " prop value: " + assocData.propValue + " uuid: " + assocData.uuid);
                    } else {
                        node.createAssociation(targetNode, assocData.assocType);
                    }
                }

            }
        },
        childGroupExists: function (group, childShortName) {
            var childGroup = this.getChildGroupByNameOrNullNotFound(group, childShortName);
            return childGroup != null;
        },
        getChildGroupByNameOrNullNotFound: function (group, childShortName) {
            var maxItems = 200;
            var skipCount = 0;
            var childGroups = group.getChildGroups(maxItems, skipCount);

            for each (var childGroup in childGroups) {
                if (childGroup.shortName == childShortName) {
                    return childGroup;
                }
            }
            return null;
        },
        createGroup: function (groupObj) {
            var groupName = groupObj.name;
            var groupDisplayName = groupObj.displayName;
            var createIfNotExists = groupObj.createIfNotExists;

            var targetGroup = groups.getGroup(groupName);

            if (!targetGroup) {
                if (createIfNotExists == 'true') {
                    if (groupDisplayName.length() == 0) {
                        groupDisplayName = groupName;
                    }
                    targetGroup = groups.createRootGroup(groupName, groupDisplayName)
                } else {
                    logger.error(this.parserScriptName + " cannot fill group: "
                        + groupName + ", because this group doesn't exists and "
                        + "parameter (createIfNotExists) not equals (true)");
                    return null;
                }
            }
            return targetGroup;
        },
        fillNodeTitle: function (node) {
            if (parser.parserData.titles.en.length > 0) {
                utils.setLocale("en");
                node.properties['cm:title'] = parser.parserData.titles.en;
                node.save();

                utils.setLocale("en_US");
                node.properties['cm:title'] = parser.parserData.titles.en;
                node.save();
            }
            if (parser.parserData.titles.ru.length > 0) {
                utils.setLocale("ru_RU");
                node.properties['cm:title'] = parser.parserData.titles.ru;
                node.save();
            }
            parser.parserData.titles.en = "";
            parser.parserData.titles.ru = "";
        },
        fillTitleData: function (propValue, prop) {
            if (parser.parserData.cmTitleRuFromProp && parser.parserData.cmTitleRuFromProp.toString().length > 0) {
                if (parser.parserData.cmTitleRuFromProp.toString() == prop.name().toString()) {
                    parser.parserData.titles.ru = propValue;
                }
            }

            if (parser.parserData.cmTitleEnFromProp && parser.parserData.cmTitleEnFromProp.toString().length > 0) {
                if (parser.parserData.cmTitleEnFromProp.toString() == prop.name().toString()) {
                    parser.parserData.titles.en = propValue;
                }
            }

            if (prop.name().toString() == "cm_title_ru") {
                parser.parserData.titles.ru = propValue;
            }

            if (prop.name().toString() == "cm_title_en") {
                parser.parserData.titles.en = propValue;
            }
        },
        getCmNameValue: function(propValue, prop) {
            var cmNameValue = "";
            if (parser.parserData.cmNameFromProp && parser.parserData.cmNameFromProp.toString().length > 0) {
                if (parser.parserData.cmNameFromProp.toString() == prop.name().toString()){
                    if (parser.parserData.cmNamePrefix && parser.parserData.cmNamePrefix.toString().length > 0) {
                        cmNameValue = parser.parserData.cmNamePrefix.toString();
                    }
                    var correctPropValue = propValue.split(".").join("");
                    correctPropValue = correctPropValue.split("/").join("-");

                    cmNameValue = cmNameValue + correctPropValue;
                }
            }
            return cmNameValue;
        },
        getUuidValue: function (propValue, prop) {
            var uuidValue = "";
            if (parser.parserData.uuidFromProp && parser.parserData.uuidFromProp.toString().length > 0) {
                if (parser.parserData.uuidFromProp.toString() == prop.name().toString()){
                    if (parser.parserData.uuidPrefix && parser.parserData.uuidPrefix.toString().length > 0) {
                        uuidValue = parser.parserData.uuidPrefix.toString();
                    }
                    var correctUuid = propValue.split(' ').join('-').toLowerCase();
                    correctUuid = correctUuid.split(".").join("");
                    correctUuid = correctUuid.split("_").join("-");
                    correctUuid = correctUuid.split(",").join("");

                    uuidValue = uuidValue + correctUuid
                }
            }
            return uuidValue;
        },
        /*TODO: fix a long search, while a large number of children*/
        getChildByProperty: function (parent, property, value) {
            var children = parent.children;

            for each (var child in children) {
                if (child.properties[property] == value) {
                    return child;
                }
            }
            return null;
        },
        getRootNodeByPath: function (path) {
            var rootNode = search.selectNodes(path)[0];
            if (!rootNode) {
                logger.error(this.parserScriptName + " cannot find root node by path: " + path);
            }
            return rootNode;
        },
        uuidExists: function (propObj) {
            var uuidExists = false;
            if (propObj['sys:node-uuid']) {
                var node = search.findNode("workspace://SpacesStore/" + propObj['sys:node-uuid']);
                if (node) {
                    uuidExists = true
                }
            }
            return uuidExists;
        },
        cmNameExists: function(path, propObj) {
            var cmNameExists = false;
            if (propObj['cm:name']) {
                var root = this.getRootNodeByPath(path);
                if (root) {
                    var node = root.childByNamePath(propObj['cm:name']);
                    if (node) {
                        cmNameExists = true;
                    }
                }
            }
            return cmNameExists;
        },
        millisToMinAndSeconds: function(millis) {
            var minutes = Math.floor(millis / 60000);
            var seconds = ((millis % 60000) / 1000).toFixed(0);
            return minutes + ":" + (seconds < 10 ? '0' : '') + seconds;
        }
    }
};

function setStatusAsync(node, status) {
    batchExecuter.processArray({
        items: [node],
        batchSize: 1,
        threads: 1,
        onNode: function(row) {
            row.properties["xni:status"] = status;
            row.save();
        }
    });
}
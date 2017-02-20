/*
var nodes = [{
    type: 'cm:content',
    props: {
        'cm:title': {ru_EN: 'русский заголовок', en_GB: 'english title'}
        'cm:name': 'name'
    }
}]
*/

function importNodes(parentPath, data) {

    var parent = (search.selectNodes(parentPath) || [])[0];
    if (!parent) {
        logger.warn("[import-nodes.js] Parent is not found! Path: " + parentPath);
        return;
    }

    for (var idx in data) {
        var nodeData = data[idx];
        var node = _getOrCreateNode(parent, nodeData.props['cm:name'], nodeData.type);
        if (!node) continue;

        for (var prop in nodeData.props) {
            _setProp(node, prop, nodeData.props[prop]);
        }

        node.save();
    }
}

function _getOrCreateNode(parent, name, type) {

    if (!type) {
        logger.warn("[import-nodes.js] node type is mandatory parameter! name: " + name);
        return null;
    }

    var node;

    if (name) {
        node = parent.childByNamePath(name);
        if (!node) {
            node = parent.createNode(name, type);
        }
    } else {
        node = parent.createNode(null, type);
    }

    return node;
}

function _setProp(node, prop, value) {

    if (value !== null && typeof value === 'object') {
        var toConvert = ["ru", "ru_RU"];
        for (var i in toConvert) {
            var locale = toConvert[i];
            if (value[locale]) {
                value[locale] = citeckUtils.toUTF8(value[locale]);
            }
        }
        return citeckUtils.setMLText(node, prop, value);
    } else if (typeof value == 'string' || value instanceof String) {
        node.properties[prop] = value;
    } else {
        node.properties[prop] = null;
    }
}

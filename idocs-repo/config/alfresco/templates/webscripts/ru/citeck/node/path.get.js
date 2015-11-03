
var nodeRef = args['nodeRef'];

function getPath() {
    var path = '';
    var node = search.findNode('node', nodeRef.replace(/:\//g,'').split('/'));
    if (node) {
        path = node.getQnamePath();
    }
    return path;
}

model.path = nodeRef? getPath() : '';
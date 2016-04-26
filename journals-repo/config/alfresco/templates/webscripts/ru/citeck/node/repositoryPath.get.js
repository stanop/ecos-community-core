
var nodeRef = args.nodeRef;

var caseNode = search.findNode(nodeRef);
var path="";
if(caseNode)
{
    var node=caseNode;
    var names = [];
    if(caseNode.isSubType("cm:folder"))
    {
        names.push(caseNode.getName());
    }
    while(node.getParent())
    {
        names.push(node.getParent().getName());
        node=node.getParent();
    }
    logger.warn(names);
    var reverseNames=names.reverse();
    logger.warn(reverseNames);
    for (var i=2; i<reverseNames.length; i++)
    {
        path=path+"/"+reverseNames[i];
    }
    logger.warn(path);
}

model.path = path;
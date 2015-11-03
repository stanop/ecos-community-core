var parentNodeRef = '',
	childNodeRef = args.child;
if (childNodeRef) {
	var childNode = search.findNode(childNodeRef);
	if (childNode && childNode.parent)
		parentNodeRef = childNode.parent.nodeRef.toString();
}
model.data = parentNodeRef;

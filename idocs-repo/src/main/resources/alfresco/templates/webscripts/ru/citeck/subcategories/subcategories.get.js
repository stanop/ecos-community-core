(function()
{
    var nodeRef = args['nodeRef'];
    var category = classification.getCategory(nodeRef);
    model.nodes = category ? category.subCategories : [];
})()
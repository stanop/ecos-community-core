(function() {
    var nodeRef = args.nodeRef;
    if (!nodeRef) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'nodeRef' must be specified");
        return null;
    }
    var node = search.findNode(nodeRef);
    if (!node) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node: " + nodeRef);
        return null;
    }

    var cardMode = args.mode || null;

    var cardletsList = cardlets.queryCardlets(node, cardMode);

    cache.maxAge = args.cacheMaxAge || 300;

    var renderCardlet = function (cardlet) {
        var availableInMobile = cardlet.properties["cardlet:availableInMobile"];
        if (availableInMobile == null) {
            availableInMobile = true;
        }
        var positionIndexInMobile = cardlet.properties["cardlet:positionIndexInMobile"];
        if (positionIndexInMobile == null) {
            positionIndexInMobile = -1;
        }
        return {
            regionId: cardlet.properties["cardlet:regionId"],
            regionColumn: cardlet.properties["cardlet:regionColumn"],
            regionPosition: cardlet.properties["cardlet:regionPosition"],
            availableInMobile: availableInMobile,
            positionIndexInMobile: positionIndexInMobile,
            cardMode: cardlet.properties["cardlet:cardMode"]
        }
    };

    model.result = {
        nodeRef: node.nodeRef.toString(),
        type: node.typeShort,
        cardMode: args.cardMode || '',
        cardlets: cardletsList.map(renderCardlet)
    }
})();
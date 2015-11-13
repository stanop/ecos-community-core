(function() {
    var routesRootFolder = search.findNode("workspace://SpacesStore/idocs-routes"),
        routes = [];

    if (routesRootFolder) {
        if (args.nodeRef) {
            var node = search.findNode(args.nodeRef);
            if (node) {
                if (node.typeShort == "route:route") {
                    routes.push(node);
                } else {
                    var type = node.properties["tk:type"] || node.properties["tk:appliesToType"], 
                        kind = node.properties["tk:kind"] || node.properties["tk:appliesToKind"];

                    var routeNodes = routesRootFolder.children;

                    if (kind) {
                        var filteredKindRoutes = filterKind(routeNodes, kind);
                        if (filteredKindRoutes) routes = filteredKindRoutes;
                    }

                    if (routes.length == 0) {
                        if (type) {
                            var filteredTypeRoutes = filterType(routeNodes, type);
                            if (filteredTypeRoutes) routes = filteredTypeRoutes;
                        }
                    }

                }
            } else {
                status.setCode(404);
                status.message = "Can't find node with nodeRef: " + args.nodeRef;
                return;
            }
        } else {
            routes = routesRootFolder.children;
        }

        model.data = routes;
        model.canCreate = routesRootFolder.hasPermission("CreateChildren");
    } else {
        status.setCode(500);
        status.message = "Can't find root folder of routes.";
        return;
    }


    // PRIVATE FUNCTIONS

    function filter(routes, options) {
        var result = [];

        if (options && options.property && options.value) {
            for (var r in routes) {
                var routeProperty = routes[r].properties[options.property];
                if (routeProperty) {
                    for (var rp in routeProperty) {
                        if (routeProperty[rp].equals(options.value)) {
                            result.push(routes[r]);
                            break;
                        }
                    }
                }
            }
        } else {
            result = routes;
        }

        return result;
    }

    function filterKind(routes, kind) {
        return filter(routes, { property: "tk:appliesToKind", value: kind });
    }

    function filterType(routes, type) {
        return filter(routes, { property: "tk:appliesToType", value: type });
    }
})()
var routeFolder = search.findNode("workspace://SpacesStore/idocs-routes");

try {
    if (!!routeFolder) {
        var routeProps = [];
        var routeName = "internal-confirmation-by-ceo";
        routeProps["tk:appliesToType"] = "workspace://SpacesStore/orders-cat-doctype-internal";
        var route = routeFolder.createNode(routeName, "route:route", routeProps)
        if (!!route) {
            var stageProps = [];
            var stageName = "GROUP_company_director";
            stageProps["route:dueDateExpr"] = "8/h";
            stageProps["cm:position"] = "1";
            var routeStage = route.createNode(stageName, "route:stage", stageProps, "route:stages");
            if (!!routeStage) {
                var participantProps = [];
                var participantName = "GROUP_company_director";
                participantProps["cm:position"] = "1";
                routeParticipant = routeStage.createNode(participantName, "route:participant", participantProps, "route:participants");

                var groupCEO = people.getGroup("GROUP_company_director");

                if (!groupCEO) {
                    logger.warn("[orders-routes.js] GROUP_company_director doesn't found! Try to take GROUP__orgstruct_home_ instead");
                    groupCEO = people.getGroup("GROUP__orgstruct_home_");
                }

                if (groupCEO) {
                    routeParticipant.createAssociation(groupCEO, "route:authority");
                    route.properties["route:precedence"] = groupCEO.nodeRef + "_" + stageProps["route:dueDateExpr"];
                    route.save();
                } else {
                    logger.warn("[orders-routes.js] GROUP__orgstruct_home_ doesn't exists!");
                }
            }
        }
    }
} catch (e) {
    logger.error("[orders-routes.js] Error when creating basic route for internal doc case");
    throw e;
}

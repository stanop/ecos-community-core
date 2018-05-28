var routeFolder = search.findNode("workspace://SpacesStore/idocs-routes");

try {
    if (!!routeFolder) {
        var routeProps = [];
        var routeName = "contract-confirmation-by-ceo";
        routeProps["tk:appliesToType"] = "workspace://SpacesStore/contracts-cat-doctype-contract";
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

                var groupCEO = search.selectNodes("/sys:system/sys:authorities/cm:GROUP_company_director");

                if (!!groupCEO && groupCEO.length > 0) {
                    routeParticipant.createAssociation(groupCEO[0], "route:authority");

                    route.properties["route:precedence"] = groupCEO[0].nodeRef + "_" + stageProps["route:dueDateExpr"];
                    route.save();
                }
            }
        }
    }
} catch (e) {
    logger.error("Error when creating basic route for contract case");
}
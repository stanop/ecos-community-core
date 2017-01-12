(function() {
    var routeRootFolder = search.findNode("workspace://SpacesStore/idocs-routes");
    if (routeRootFolder) {
        var jsonRoute = json.get("route"), 
            jsonStages = jsonRoute.getJSONArray("stages");

        if (!jsonRoute) {
            status.setCode(400);
            return;
        }

        if (!jsonStages || jsonStages.length() == 0) {
            status.setCode(400);
            return;
        }

        // create route
        var newRoute = routeRootFolder.createNode(jsonRoute.get("name"), "route:route");

        for (var js = 0; js < jsonStages.length(); js++) {
            var jsonParticipants = jsonStages.getJSONObject(js).getJSONArray("participants");
            if (jsonParticipants.length() > 0) {
                
                // create stages
                var newStage = newRoute.createNode(null, "route:stage", "route:stages");

                if (jsonStages.getJSONObject(js).has("dueDateExpr")) newStage.properties["route:dueDateExpr"] = jsonStages.getJSONObject(js).get("dueDateExpr");
                if (jsonStages.getJSONObject(js).has("displayName")) newStage.properties["cm:displayName"] = jsonStages.getJSONObject(js).get("displayName");
                if (jsonStages.getJSONObject(js).has("position")) {
                    newStage.properties["cm:position"] = jsonStages.getJSONObject(js).get("position");
                    newStage.properties.name = "stage-" + jsonStages.getJSONObject(js).get("position");
                }

                newStage.save();

                // create participants
                for (var jp = 0; jp < jsonParticipants.length(); jp++) {
                    // find authority for participant
                    var authorityNode = search.findNode(jsonParticipants.getJSONObject(jp).get("nodeRef"));
                    if (authorityNode) {
                        var name = authorityNode.properties["cm:userName"] || authorityNode.properties["cm:name"],
                            newParticipant = newStage.createNode(name, "route:participant", "route:participants");

                        // create association
                        newParticipant.createAssociation(authorityNode, "route:authority");

                        // special properties
                        if (jsonParticipants.getJSONObject(jp).has("position")) newParticipant.properties["cm:position"] = jsonParticipants.getJSONObject(jp).get("position");

                        newParticipant.save();
                    }
                }
            } 
        }

        model.data = newRoute;
    } else {
        status.setCode(500);
        return;
    }
})()

// TODO: precedence
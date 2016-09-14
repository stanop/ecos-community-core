(function() {
    if (!json.has("field_names")) {
        status.setCode(400);
        status.message = "Property \"field_names\" not found";
        return;
    }

    var fields = json.get("field_names").split(","),
        stagesFieldName = fields[0],
        participantListFieldName = fields[1],
        precedenceFieldName = fields[2];

    if (!json.has(stagesFieldName)) {
        status.setCode(400);
        status.message = "Property \"" + stagesFieldName + "\" not found";
        return;
    }

    if (!json.has(precedenceFieldName)) {
        status.setCode(400);
        status.message = "Property \"" + precedenceFieldName + "\" not found";
        return;
    }

    if (!json.has(participantListFieldName)) {
        status.setCode(400);
        status.message = "Property \"" + participantListFieldName + "\" not found";
        return;
    }

    if (!json.has("prop_cm_name")) {
        status.setCode(400);
        status.message = "Property \"prop_cm_name\" not found";
        return;
    }

    var stages = eval(json.get(stagesFieldName) + ""),
        precedence = json.get(precedenceFieldName),
        participantList = json.get(participantListFieldName),
        name = json.get("prop_cm_name"),
        itemId = "", type = "", kind = "", taggable = "";

    var routeScript = "";
    if (json.has("prop_route_scriptCondition")) {
        routeScript = json.get("prop_route_scriptCondition");
    }

    if (json.has("nodeRefItemId")) {
        itemId = json.get("nodeRefItemId");
    }


    if (json.has("prop_tk_appliesToType")) {
        type = json.get("prop_tk_appliesToType");
    }

    if (json.has("prop_tk_appliesToKind")) {
        kind = json.get("prop_tk_appliesToKind");
    }

    if (json.has("prop_cm_taggable")) {
        taggable = json.get("prop_cm_taggable");
    }


    var routeRootFolder = search.findNode("workspace://SpacesStore/idocs-routes");
    if (routeRootFolder) {
        var route;

        if (itemId) {
            // EDIT MODE

            route = search.findNode(itemId);
            if (route) {
                var routeStages = route.childAssocs["route:stages"];

                // Change the name of the route
                if(route.properties["cm:name"] != name) {
                    route.properties["cm:name"] = name;
                }

                // Change route script condition
                if (route.properties["route:scriptCondition"] != routeScript) {
                    route.properties["route:scriptCondition"] = routeScript;
                }

                // Change the priority
                if(route.properties["route:precedence"] != precedence) {
                    route.properties["route:precedence"] = precedence;
                }

                // Update the type of document
                if (type && type != "") {
                    route.properties["tk:appliesToType"] = type.split(",");
                } else {
                    delete route.properties["tk:appliesToType"];
                }

                // Update the kind of document
                if (kind && kind != "") {
                    route.properties["tk:appliesToKind"] = kind.split(",");
                } else {
                    delete route.properties["tk:appliesToKind"];
                }

                // Update labels
                if (taggable && taggable != "") {
                    route.properties["cm:taggable"] = taggable.split(",");
                } else {
                    delete route.properties["cm:taggable"];
                }

                // Save changes
                route.save();

                // Removing unnecessary stages
                for (var rs in routeStages) {
                    if (get(stages, routeStages[rs].nodeRef.toString()) == null) {
                        routeStages[rs].remove();
                    }
                }

                for (var s in stages) {
                    var stage = stages[s];
                    if (stage.nodeRef) {
                        var routeStage = get(routeStages, stage.nodeRef);
                        if (routeStage) {
                            // If stages coincides noderef
                            var rs_nodeRef = routeStage.nodeRef.toString();

                            var routeStageParticipants = routeStage.childAssocs["route:participants"],
                                participants = stage.participants;

                            // Change the position
                            if (routeStage.properties["cm:position"] != stage.position) {
                                routeStage.properties["cm:position"] = stage.position;
                            }

                            // Changing the term
                            if (routeStage.properties["route:dueDateExpr"] != stage.dueDateExpr) {
                                routeStage.properties["route:dueDateExpr"] = stage.dueDateExpr;
                            }

                            // Save changes
                            routeStage.save();

                            // Removal of unnecessary parties
                            for (var rsp in routeStageParticipants) {
                                if (get(participants, routeStageParticipants[rsp].nodeRef.toString()) == null) {
                                    routeStageParticipants[rsp].remove();
                                }
                            }

                            for (var p in participants) {
                                var participant = participants[p];
                                if (participant.nodeRef) {
                                    var routeStageParticipant = get(routeStageParticipants, participant.nodeRef);
                                    if (routeStageParticipant) {
                                        var rsp_position = routeStageParticipant.properties["cm:position"];;

                                        // Change position
                                        if (rsp_position != participant.position) {
                                            rsp_position = participant.position;
                                        }
                                    }
                                } else {
                                    createParticipant(participant, routeStage)
                                }
                            }
                        }
                    } else {
                        // if stage has not nodeRef
                        createStage(stage, route);
                    }
                }
            } else {
                status.setCode(404);
                status.message = "Can't find route with nodeRef: " + itemId;
                return;
            }

        } else {
            // CREATE MODE
            route = routeRootFolder.createNode(name, "route:route", {
                "route:precedence":     precedence
            });

            if (type && type != "") {
                route.properties["tk:appliesToType"] = type.split(",");
            }

            if (kind && kind != "") {
                route.properties["tk:appliesToKind"] = kind.split(",");
            }

            if (taggable && taggable != "") {
                route.properties["cm:taggable"] = taggable.split(",");
            }

            if (routeScript && routeScript != "") {
                route.properties["route:scriptCondition"] = routeScript;
            }

            route.save();

            for (var s in stages) {
                createStage(stages[s], route)
            }
        }

        model.persistedObject = route.nodeRef.toString();
    } else {
        status.setCode(500);
        status.message = "Can't find root folder of routes.";
        return;
    }


    //  ----------------  //
    //  Private Function  //
    //  ----------------  //


    function createStage(stage, route) {
        var participants = stage.participants;
        if (participants.length > 0) {
            var newStage = route.createNode(null, "route:stage", "route:stages");
            if (stage.dueDateExpr)
                newStage.properties["route:dueDateExpr"] = stage.dueDateExpr;
            if (stage.position) {
                newStage.properties["cm:position"] = stage.position;
                newStage.properties.name = "stage-" + stage.position;
            }

            newStage.save();

            stage.nodeRef = newStage.nodeRef.toString();

            for (var p in participants) {
                createParticipant(participants[p], newStage);
            }
        }
    }

    function createParticipant(participant, stage) {
        var authority = participant.authority,
            authorityNode = search.findNode(authority.nodeRef);

        if (authorityNode) {
            var nameParticipant = authorityNode.properties["cm:userName"] || authorityNode.properties["cm:name"],
                newParticipant = stage.createNode(nameParticipant, "route:participant", "route:participants");

            newParticipant.createAssociation(authorityNode, "route:authority");
            if (participant.position) newParticipant.properties["cm:position"] = participant.position;
            newParticipant.save();
        }
    }

    function get(object, nodeRef) {
        for (var o in object) {
            if (object[o].nodeRef && object[o].nodeRef.toString() == nodeRef) {
                return object[o];
            }
        }
        return null;
    }
})();
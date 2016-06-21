(function() {

    if (!json.has("route")) {
        status.setCode(400);
        status.message = "Node \"route\" not found";
        return;
    }

    var route = jsonUtils.toObject(json.get("route")),
        stages = eval(route.stages.toString() + ""),
        precedence = route.precedence,
        name = route.name;


    var routeNode = search.findNode(route.nodeRef);
    if (routeNode) {
        var routeNodeStages = routeNode.childAssocs["route:stages"];

        // Change the name
        if(routeNode.properties["cm:name"] != name) {
            routeNode.properties["cm:name"] = name;
        }

        // Change the priority
        if(routeNode.properties["route:precedence"] != precedence) {
            routeNode.properties["route:precedence"] = precedence;   
        }

        // Save changes
        routeNode.save();

        //  Removing unnecessary stages
        for (var rs in routeNodeStages) {
            if (get(stages, routeNodeStages[rs].nodeRef.toString()) == null) {
                routeNodeStages[rs].remove();
            }
        }

        for (var s in stages) {
            var stage = stages[s];
            if (stage.nodeRef) {
                var routeNodeStage = get(routeNodeStages, stage.nodeRef);
                if (routeNodeStage) {
                    // If stage has noderef
                    var rs_nodeRef = routeNodeStage.nodeRef.toString();

                    var routeNodeStageParticipants = routeNodeStage.childAssocs["route:participants"],
                        participants = stage.participants;

                    // Change the position
                    if (routeNodeStage.properties["cm:position"] != stage.position) {
                        routeNodeStage.properties["cm:position"] = stage.position;
                    }

                    // Change the term
                    if (routeNodeStage.properties["route:dueDateExpr"] != stage.dueDateExpr) {
                        routeNodeStage.properties["route:dueDateExpr"] = stage.dueDateExpr;
                    }

                    // Save changes
                    routeNodeStage.save();

                    // Removal of unnecessary parties
                    for (var rsp in routeNodeStageParticipants) {
                        if (get(participants, routeNodeStageParticipants[rsp].nodeRef.toString()) == null) {
                            routeNodeStageParticipants[rsp].remove();
                        }
                    }

                    for (var p in participants) {
                        var participant = participants[p];
                        if (participant.nodeRef) {
                            var routeNodeStageParticipant = get(routeNodeStageParticipants, participant.nodeRef);
                            if (routeNodeStageParticipant) {
                                var rsp_position = routeNodeStageParticipant.properties["cm:position"];

                                // Change the position
                                if (rsp_position != participant.position) {
                                    rsp_position = participant.position;
                                } 
                            }
                        } else {
                            createParticipant(participant, routeNodeStage)
                        }
                    }
                } 
            } else {
                // if stage has no nodeRef
                createStage(stage, routeNode);
            }
        }

        model.data = routeNode;
    } else {
        status.setCode(404);
        status.message = "Can't find route with nodeRef: " + route.nodeRef;
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
})()
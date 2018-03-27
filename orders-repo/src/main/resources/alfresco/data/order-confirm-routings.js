var routings = [
    {
        "name": "Internal standard confirmation",
        //"kind": "workspace://SpacesStore/cat-order-std",
        "authorities": ["GROUP_company_director"]
    }
];


for (var i = 0; i < routings.length; i++) {
    createRouting(routings[i].name, routings[i].kind, routings[i].condition, routings[i].authorities);
}

function createRouting(name, kind, condition, authorities) {
    var data =
        {
            "alf_destination": "workspace://SpacesStore/idocs-routes",
            "prop_cm_name": name,
            "prop_tk_appliesToType": "workspace://SpacesStore/orders-cat-doctype-internal",
            "prop_tk_appliesToKind": kind,
            "stage-1-time": "",
            "stage-1-time-type": "h",
            "field_names": "route_object_field,participant_list_field,prop_route_precedence",
            "prop_cm_taggable": ""
        },
        obj_field_participants = [],
        precedence = "",
        participants_list = "";

    for (var i = 0; i < authorities.length; i++) {
        var authorityData = getAuthorityData((i+1)+"", authorities[i]);
        for (var k in authorityData.data) {
            data[k] = authorityData.data[k];
        }
        obj_field_participants.push(authorityData.obj_data);

        if (precedence.length == 0) {
            precedence = authorityData.nodeRef + "_0/h";
            participants_list = authorityData.nodeRef + "";
        } else {
            precedence += '|' + authorityData.nodeRef;
            participants_list += "," + authorityData.nodeRef;
        }
    }
    data['participant_list_field'] = participants_list;
    data['prop_route_precedence'] = precedence;

    if (condition != null && condition != undefined) {
        data["prop_route_scriptCondition"] = condition;
    }

    data["route_object_field"] = [{
        'position': 1,
        'participants': obj_field_participants,
        'dueDateExpr': '/h'
    }];

    create(data);

    function getAuthorityData(idx, authority) {
        var authorityNode = authority.indexOf("GROUP_") == 0? people.getGroup(authority) : people.getPerson(authority);
        var data = {};
        var displayName = authorityNode.properties['cm:authorityDisplayName'] || authority;

        data["stage-1-participant-"+idx+"-search"] = "";
        data["stage-1-participant-"+idx] = authorityNode.nodeRef + "";

        var obj_data = {
            'position': idx,
            'authority': {
                'nodeRef': authorityNode.nodeRef + "",
                "displayName": displayName
            }
        };

        return {'data':data, 'obj_data':obj_data, 'nodeRef': authorityNode.nodeRef + ""};
    }
}


function create(json) {
    if (!json["field_names"]) {
        return;
    }

    var fields = json["field_names"].split(","),
        participantListFieldName = fields[1],
        precedenceFieldName = fields[2];

    if (!json[precedenceFieldName]) {
        return;
    }

    if (!json[participantListFieldName]) {
        return;
    }

    if (!json["prop_cm_name"]) {
        return;
    }

    var stages = json['route_object_field'],
        precedence = json[precedenceFieldName],
        participantList = json[participantListFieldName],
        name = json["prop_cm_name"],
        itemId = "", type = "", kind = "", taggable = "";

    var routeScript = "";
    if (json["prop_route_scriptCondition"]) {
        routeScript = json["prop_route_scriptCondition"];
    }

    if (json["nodeRefItemId"]) {
        itemId = json["nodeRefItemId"];
    }


    if (json["prop_tk_appliesToType"]) {
        type = json["prop_tk_appliesToType"];
    }

    if (json["prop_tk_appliesToKind"]) {
        kind = json["prop_tk_appliesToKind"];
    }

    if (json["prop_cm_taggable"]) {
        taggable = json["prop_cm_taggable"];
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
                if (route.properties["cm:name"] != name) {
                    route.properties["cm:name"] = name;
                }

                // Change route script condition
                if (route.properties["route:scriptCondition"] != routeScript) {
                    route.properties["route:scriptCondition"] = routeScript;
                }

                // Change the priority
                if (route.properties["route:precedence"] != precedence) {
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
                                        var rsp_position = routeStageParticipant.properties["cm:position"];
                                        ;

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
                return;
            }

        } else {
            // CREATE MODE
            route = routeRootFolder.createNode(name, "route:route", {
                "route:precedence": precedence
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
}
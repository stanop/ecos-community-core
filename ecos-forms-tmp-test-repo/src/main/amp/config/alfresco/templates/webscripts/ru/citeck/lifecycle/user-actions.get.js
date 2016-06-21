/**
 * Created by alexander.nemerov on 06.03.14.
 */

(function() {
    var nodeRef = args.nodeRef;
    var actions = lifecycle.getAvailableUserEvents(nodeRef);
    model.actions = [];
    logger.log("Actions collection size: " + actions.size());
    for (var i = 0; i < actions.size(); i++) {
        logger.log(actions.get(i).event.eventParams);
        model.actions.push({
            "node" : nodeRef,
            //"actionParams" : eval('(' + actions.get(i).event.eventParams + ')')
            "eventType" : actions.get(i).event.eventType,
            "actionParams" : actions.get(i).event.eventParams
        });
    }
})();
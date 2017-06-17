(function() {
    
    var document = search.findNode(args.nodeRef);
    if(!document) {
        status.setCode(status.STATUS_NOT_FOUND, "Can not find node " + args.nodeRef);
        return;
    }
    
    var query = 'TYPE:"history:basicEvent"';
    var mapping = {
        'events': 'event:name'
    };

    var filterProperties = {};

    for (var name in args) {
        if (name == 'nodeRef') {
            continue;
        }
        var values = args[name].split(',');
        var realName = mapping[name] || name;
        if (!realName || realName.indexOf(':') == -1) {
            continue;
        }
        filterProperties[realName] = values;
    }

    var events = document.sourceAssocs['event:document'] || [];

    if (args.showEventForSubCases) {
        events = events.concat(document.sourceAssocs['event:case'] || []);
    }

    var filteredEvents = [];

    for (var i in events) {
        var event = events[i];
        if (checkProperties(event, filterProperties)) {
            filteredEvents.push(event);
        }
    }

    model.events = filteredEvents.sort(function(e1, e2) {
        var d1 = e1.properties['event:date'],
            d2 = e2.properties['event:date'],
            t1 = d1 ? d1.getTime() : 0,
            t2 = d2 ? d2.getTime() : 0;
            var result;
            if(args.sort=='desc')
                result = t2 - t1;
            else
                result = t1 - t2;
        return result;
    });
})();

function checkProperties(node, requiredProperties) {
    for (var property in requiredProperties) {
        var nodeValue = node.properties[property];
        var requiredValues = requiredProperties[property];
        if (requiredValues.indexOf(nodeValue) == -1) {
            return false;
        }
    }
    return true;
}
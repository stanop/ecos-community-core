(function() {

    function jsonGet(url) {
        var result = remote.connect("alfresco").get(url);

        try {
            if (result.status == 200) {
                var json = eval('(' + result + ')');
                if (json != null) return json;
            }
        } catch (e) {
            return null;
        }

        return null;
    }

    function isPendingUpdate(nodeRef) {
        var isPendingUpdate = jsonGet('/citeck/node/check-pending-update?nodeRef=' + nodeRef);
        return isPendingUpdate != null ? isPendingUpdate : false;
    }

    model.nodeRef = args.nodeRef;
    model.isPendingUpdate = isPendingUpdate(model.nodeRef);

})();
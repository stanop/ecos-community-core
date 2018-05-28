<import resource="classpath:alfresco/module/idocs-repo/scripts/case-mt/utils.js">

const ROLE_CONFIRMERS = "confirmers";
const ROLE_INITIATOR = "initiator";
const ROLE_SIGNER = "signer";
const ROLE_OWNER = "owner";
const ROLE_PERFORMERS = "performers";
const ROLE_ADDITIONAL_CONFIRMERS = "additional-confirmers";
const ROLE_TECHNOLOGIST = "technologist";

var roles = {
    rolesData: {},

    getConfirmersRefsFromRouting: function(type, kind) {
        var filterRoutings = function(routings) {
            var result = [];
            for (var i = 0; i < routings.length; i++) {
                var condition = routings[i].properties['route:scriptCondition'];
                if (condition != null && condition != "") {
                    var conditionResult;
                    try {
                        conditionResult = eval(condition);
                    } catch (e) {
                        logger.warn("Condition evaluation failed.");
                        logger.warn("MESSAGE: " + e);
                        logger.warn("CONDITION: " + condition);
                        conditionResult = false;
                    }
                    if (conditionResult) {
                        result.push(routings[i]);
                    }
                } else {
                    result.push(routings[i]);
                }
            }
            return result;
        };
        var getRouting = function(type, kind) {
            var query = 'TYPE:"route:route" AND @tk\\:appliesToType:"' + type.nodeRef + '"';

            if (kind) {
                query += ' AND @tk\\:appliesToKind:"' + kind.nodeRef + '"';
            } else {
                query += ' AND (ISNULL:"tk:appliesToKind" OR ISUNSET:"tk:appliesToKind")';
            }

            var routings = search.query({'query': query, 'language': 'fts-alfresco'}) || [];
            if (routings.length == 0 && kind) {
                routings = getRouting(type, null);
            }
            return filterRoutings(routings);
        };

        if (!type && !kind) return {};

        var confirmers = new Packages.java.util.ArrayList();

        var routings = getRouting(type, kind);
        for (var i = 0; i < routings.length; i++) {
            var precedence = routings[i].properties["route:precedence"];
            var routeData = Packages.ru.citeck.ecos.workflow.confirm.PrecedenceToJsonListener.convertPrecedence(precedence);
            try {
                for (var i; i < routeData.stages.size(); i++) {
                    confirmers.addAll(routeData.stages.get(i).confirmers);
                }
            } catch (e) {}
        }

        var confirmersRefs = {};
        for (var i = 0; i < confirmers.size(); i++) {
            confirmersRefs[confirmers.get(i)['nodeRef']] = true;
        }
        return confirmersRefs;
    },

    getAssigneesByName: function(roleName) {
        if (!roleName) return [];
        var data = roles.rolesData[roleName];
        return data ? data.fn() : [];
    },

    getAssignees: function() {
        if (!role) return [];
        var roleName = role.properties['icaseRole:varName'];
        var data = roles.rolesData[roleName];
        return data ? data.fn() : [];
    }
};

(function() {
    roles.rolesData[ROLE_CONFIRMERS] = {
        fn: function () {
            logger.warn('Fill confirmers role');
            var type = document.properties['tk:type'];
            var kind = document.properties['tk:kind'];
            var confirmersRefs = {};
            var confirmersAssocs = document.assocs["orders:confirmers"];
            if (confirmersAssocs && confirmersAssocs.length > 0) {
                for (var i in confirmersAssocs) {
                    confirmersRefs[confirmersAssocs[i].nodeRef] = true;
;                }
            } else {
                confirmersRefs = roles.getConfirmersRefsFromRouting(type, kind);
            }

            var result = [];
            for (var ref in confirmersRefs) {
                result.push(search.findNode(ref));
            }
            return result;
        }
    };
    roles.rolesData[ROLE_INITIATOR] = {
        fn: function () {
            logger.warn('Fill initiator role');
            var initiator = (document.assocs['idocs:performer'] || [])[0];
            if (!initiator) {
                var creator = document.properties['cm:creator'];
                if (creator) {
                    initiator = people.getPerson(creator);
                }
            }
            return initiator ? [initiator] : [];
        }
    };
    roles.rolesData[ROLE_SIGNER] = {
        fn: function () {
            logger.warn('Fill signer role');
            var signer = (document.assocs['idocs:signatory'] || [])[0];
            return signer ? [signer] : [];
        }
    };
    roles.rolesData[ROLE_OWNER] = {
        fn: function () {
            logger.warn('Fill supervisor role');
            var owner = (document.assocs['idocs:supervisor'] || [])[0];
            return owner ? [owner] : [];
        }
    };
    roles.rolesData[ROLE_TECHNOLOGIST] = {
        fn: function () {
            var technologist = [];
            technologist.push(people.getGroup("GROUP_orders-technologist"));
            return technologist;
        }
    }
})();

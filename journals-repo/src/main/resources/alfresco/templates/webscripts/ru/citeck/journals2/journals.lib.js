importPackage(Packages.ru.citeck.ecos.search);
importPackage(Packages.java.util);

var journalsLib = {

    renderJournalType: function (journalType) {
        return {
            id: journalType.getId(),
            settings: this.renderOptions(journalType.getOptions()),
            groupActions: this.renderGroupActions(journalType.getGroupActions()),
            attributes: this.renderAttributes(journalType)
        };
    },
    
    renderAttributes: function (journalType) {
        var result = [];
        var attributes = journalType.getAttributes();
        for (var i in attributes) {
            var attribute = attributes[i];
            result.push({
                name: attribute,
                isDefault: journalType.isAttributeDefault(attribute),
                visible: journalType.isAttributeVisible(attribute),
                searchable: journalType.isAttributeSearchable(attribute),
                sortable: journalType.isAttributeSortable(attribute),
                groupable: journalType.isAttributeGroupable(attribute),
                settings: this.renderOptions(journalType.getAttributeOptions(attribute)),
                batchEdit: this.renderBatchEdit(journalType.getBatchEdit(attribute)),
                criterionInvariants: this.renderInvariants(journals.getCriterionInvariants(journalType.getId(), attribute))
            });
        }
        return result;
    },

    renderInvariants: function (invariants) {

        var result = [];
        var getExpression = function (value) {

            if (value instanceof SearchCriteria) {
                var triplets = value.getTriplets();
                var criteriaResult = [];
                for (var i = 0; i < triplets.size(); i++) {
                    criteriaResult.push({
                        "attribute": triplets.get(i).field,
                        "predicate": triplets.get(i).predicate,
                        "value": triplets.get(i).value
                    })
                }
                return criteriaResult;
            } else if (value instanceof List) {
                var items = [];
                for (var i = 0; i < value.size(); i++) {
                    items.push(value.get(i));
                }
                return items;
            }
            return value;
        };
        for (var i = 0; i < invariants.length; i++) {
            var invariant = invariants[i];
            result.push({
                feature: invariant.getFeature(),
                language: invariant.getLanguage(),
                expression: getExpression(invariant.getValue())
            });
        }
        return result;
    },

    renderBatchEdit: function (batchEdit) {
        var result = [];
        for (var i = 0; i < batchEdit.size(); i++) {
            var edit = batchEdit.get(i);
            result.push({
                id: edit.getId(),
                label: edit.getTitle(),
                func: "onBatchEdit",
                isDoclib: false,
                settings: this.renderOptions(edit.getOptions())
            });
        }
        return result;
    },

    renderGroupActions: function (actions) {
        var result = [];
        for (var i = 0; i < actions.size(); i++) {
            var action = actions.get(i);
            result.push({
                id: "group-action-" + i,
                label: action.getTitle(),
                func: "onGroupAction",
                isDoclib: false,
                settings: this.renderOptions(action.getOptions()),
                actionId: action.getId()
            });
        }
        return result;
    },

    renderOptions: function (options) {
        var result = {};
        for (var key in options) {
            result[key] = options[key];
        }
        return result;
    },

    renderCriteria: function (criteria) {
        return {
            template: template,
            params: this.renderOptions(criteria.getParams()),
            regions: this.renderCriteriaRegions(criteria.getRegions())
        };
    },

    renderCriteriaRegions: function (regions) {
        var result = [];
        for (var name in regions) {
            var region = regions[name];
            result[name] = {
                template: region.getTemplate(),
                params: region.getParams()
            };
        }
        return result;
    }

};

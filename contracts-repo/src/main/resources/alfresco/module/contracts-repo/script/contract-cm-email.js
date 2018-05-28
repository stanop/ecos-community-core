var email = {

    formatters: {
        byType: {
            'cm:person': function (user) {
                var firstName = user.properties['cm:firstName'];
                var lastName = user.properties['cm:lastName'];
                return (firstName ? firstName + " " : "") + (lastName ? lastName : "");
            },
            'idocs:legalEntity': function (le) {
                return le.properties['idocs:fullOrganizationName'] || "";
            }
        },
        def: function (node) {
            return node.properties['cm:title'] || "";
        }
    },

    format: function (node) {
        if (!node) return "";
        var formattersByType = this.formatters.byType;
        for (var type in formattersByType) {
            if (node.isSubType(type)) {
                return formattersByType[type](node);
            }
        }
        return this.formatters.def(node);
    },

    getAttribute: function(node, attData, props) {

        var value = props[attData.att];
        if (!value) {
            value = node.properties[attData.att];
        }
        if (!value) {
            var assocs = node.assocs[attData.att];
            if (assocs && assocs.length > 0) {
                value = this.format(attData.multiple ? assocs : assocs[0]);
            }
        }
        var result = {value: value || ""};
        for (var k in attData) {
            result[k] = attData[k];
        }
        if (props.customTitle && props.customTitle[attData.att]) {
            result.title = props.customTitle[attData.att];
        }
        return result;
    },

    _getContractData: function() {
        return [
            {title: 'Инициатор', att: 'idocs:performer'},
            {title: 'Организация', att: 'contracts:agreementLegalEntity'},
            {title: 'Предмет', att: 'contracts_agreementSubject'},
            {title: 'Номер', att: 'contracts:agreementNumber'},
            {title: 'Наименование', att: 'cm:name', link: '/page/document-details?nodeRef=' + document.nodeRef},
            {title: 'Статус', att: 'icase:caseStatusAssoc'},
            {title: 'Комментарий', att: 'comment'}
        ]
    },

    prepareTemplateData: function(props) {
        var attributes = [];
        var data = this._getContractData();
        for (var i in data) {
            attributes.push(this.getAttribute(document, data[i], props));
        }
        return attributes;
    },

};
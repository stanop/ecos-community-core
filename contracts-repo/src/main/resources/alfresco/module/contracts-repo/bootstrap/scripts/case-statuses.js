var statuses = [
    {name: 'draft',                title: {'en': 'Draft',                  'ru': '\u0427\u0435\u0440\u043D\u043E\u0432\u0438\u043A'}},
    {name: 'new',                  title: {'en': 'New',                    'ru': '\u041D\u043E\u0432\u044B\u0439'}},
    {name: 'deleted',              title: {'en': 'Deleted',                'ru': '\u0423\u0434\u0430\u043B\u0435\u043D'}},
    {name: 'approval',             title: {'en': 'Approve',                'ru': '\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u0435'}},
    {name: 'additional-approval',  title: {'en': 'Approve',                'ru': '\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u0435'}},
    {name: 'approved',             title: {'en': 'Approved',               'ru': '\u0421\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D'}},
    {name: 'reworking',            title: {'en': 'Reworking',              'ru': '\u0414\u043E\u0440\u0430\u0431\u043E\u0442\u043A\u0430'}},
    {name: 'signing',              title: {'en': 'Signing',                'ru': '\u041D\u0430 \u043F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0438\u0438'}},
    {name: 'counterparty-signing', title: {'en': 'Counterparty signing',   'ru': '\u041D\u0430 \u043F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0438\u0438 \u043A\u043E\u043D\u0442\u0440\u0430\u0433\u0435\u043D\u0442\u043E\u043C'}},
    {name: 'scanning',             title: {'en': 'Scanning',               'ru': '\u0421\u043A\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435'}},
    {name: 'scanned',              title: {'en': 'Scanned',                'ru': '\u041E\u0442\u0441\u043A\u0430\u043D\u0438\u0440\u043E\u0432\u0430\u043D'}},
    {name: 'active',               title: {'en': 'Active',                 'ru': '\u0414\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442'}},
    {name: 'repealed',             title: {'en': 'Repealed',               'ru': '\u0410\u043D\u043D\u0443\u043B\u0438\u0440\u043E\u0432\u0430\u043D'}},
    {name: 'request-repeal',       title: {'en': 'Request repeal',         'ru': '\u0417\u0430\u043F\u0440\u043E\u0441 \u043D\u0430 \u0410\u043D\u043D\u0443\u043B\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435'}},
    {name: 'terminated',           title: {'en': 'Terminated',             'ru': '\u041F\u0440\u0435\u043A\u0440\u0430\u0442\u0438\u043B \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435'}},
    {name: 'registered',           title: {'en': 'Registered (archive)',   'ru': '\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D (\u0430\u0440\u0445\u0438\u0432)'}}, 
    {name: 'rejected',             title: {'en': 'Rejected',               'ru': '\u041E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u0430'}},
    {name: 'signing',              title: {'en': 'Signing',                'ru': '\u041D\u0430 \u043F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0438\u0438'}},
    {name: 'on-approval',          title: {'en': 'Approve',                'ru': '\u041D\u0430 \u0441\u043E\u0433\u043B\u0430\u0441\u043E\u0432\u0430\u043D\u0438\u0438'}},
    {name: 'signed',               title: {'en': 'Signed',                 'ru': '\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0430'}},
    {name: 'prepared',             title: {'en': 'Prepared',               'ru': '\u041F\u043E\u0434\u0433\u043E\u0442\u043E\u0432\u043B\u0435\u043D\u0430'}},
    {name: 'closed',               title: {'en': 'Closed',                 'ru': '\u0417\u0430\u043A\u0440\u044B\u0442\u0430'}},
    {name: 'cancelled',            title: {'en': 'Cancelled',              'ru': '\u041E\u0442\u043C\u0435\u043D\u0435\u043D\u0430'}}
];

function main(statuses) {

    var statusNode,
        repositoryHelper = services.get('repositoryHelper'),
        companyhome = search.findNode(repositoryHelper.getCompanyHome()),
        root = (companyhome.childrenByXPath("app:dictionary/cm:dataLists/cm:case-status") || [])[0];

    if (!root) {
        logger.warn("[case-statuses.js] Case statuses root not found!");
        return;
    }

    for (var i in statuses) {
        var status = statuses[i];
        statusNode = root.childByNamePath(status.name);
        if (statusNode == null) {
            statusNode = root.createNode(status.name, '{http://www.citeck.ru/model/icase/1.0}caseStatus');
        }
        utils.setLocale("en_US");
        statusNode.properties['cm:title'] = status.title.en;
        utils.setLocale("ru_RU");
        statusNode.properties['cm:title'] = status.title.ru;
        statusNode.save();
    }
}

main(statuses);
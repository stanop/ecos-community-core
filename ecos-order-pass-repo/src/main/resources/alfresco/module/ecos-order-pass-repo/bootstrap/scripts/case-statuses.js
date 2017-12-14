var statuses = [
    {name: 'order-pass-draft',                title: {'en': 'Draft',                  'ru': '\u0427\u0435\u0440\u043D\u043E\u0432\u0438\u043A'}},
    {name: 'order-pass-new',                  title: {'en': 'New',                    'ru': '\u041D\u043E\u0432\u044B\u0439'}},
    {name: 'order-pass-ordered',            title: {'en': 'Pass Ordered',              'ru': '\u041f\u0440\u043e\u043f\u0443\u0441\u043a \u0437\u0430\u043a\u0430\u0437\u0430\u043d'}},
    {name: 'order-pass-denied', title: {'en': 'Pass Denied', 'ru': '\u041e\u0442\u043a\u0430\u0437\u0430\u043d\u043e \u0432 \u043f\u0440\u043e\u043f\u0443\u0441\u043a\u0435'}},
    {name: 'order-pass-processing', title: {'en': 'Pass Ordering', 'ru': '\u0417\u0430\u043a\u0430\u0437 \u043f\u0440\u043e\u043f\u0443\u0441\u043a\u0430'}}
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
        statusNode.save();
        utils.setLocale("ru_RU");
        statusNode.properties['cm:title'] = status.title.ru;
        statusNode.save();
    }
}

main(statuses);
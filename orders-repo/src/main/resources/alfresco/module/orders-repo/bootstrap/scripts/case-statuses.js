var statuses = [
    {name: 'inprogress',           title: {'en': 'In Progress',            'ru': '\u0412 \u043F\u0440\u043E\u0446\u0435\u0441\u0441\u0435'}},
    {name: 'onexecution',          title: {'en': 'On Execution',           'ru': '\u041D\u0430 \u0432\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0438\u0438'}},
    {name: 'infamiliarization',    title: {'en': 'In Familiarization',     'ru': '\u041D\u0430 \u043E\u0437\u043D\u0430\u043A\u043E\u043C\u043B\u0435\u043D\u0438\u0438'}}
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
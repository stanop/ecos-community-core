var data = [
    {name:"confirmers", title:"\u0421\u043E\u0433\u043B\u0430\u0441\u0443\u044E\u0449\u0438\u0435"},
    {name:"initiator", title:"\u0418\u043D\u0438\u0446\u0438\u0430\u0442\u043E\u0440"},
    {name:"scan-man", title:"\u0421\u043A\u0430\u043D\u0438\u0440\u043E\u0432\u0449\u0438\u043A"},
    {name:"archivist", title:"\u0410\u0440\u0445\u0438\u0432\u0438\u0441\u0442"},
    {name:"signer", title:"\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442"},
    {name:"owner", title:"\u041B\u0438\u043D\u0435\u0439\u043D\u044B\u0439 \u0440\u0443\u043A\u043E\u0432\u043E\u0434\u0438\u0442\u0435\u043B\u044C"},
    {name:"lawyer", title:"\u042E\u0440\u0438\u0441\u0442"},
    {name:"signer", title:"\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442"},
    {name: "auditor", title: "\u0410\u0443\u0434\u0438\u0442\u043e\u0440"},
    {name: "technologist", title: "\u0422\u0435\u0445\u043D\u043E\u043B\u043E\u0433"},
    {name: "performers", title: "\u0418\u0441\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u0438"},
    {
        name: "additional-confirmer",
        title: "\u0414\u043E\u043F. \u0441\u043E\u0433\u043B\u0430\u0441\u0443\u044E\u0449\u0438\u0439"
    }
];

function createCaseRoles() {
    path= "/app:company_home/app:dictionary/cm:dataLists/cm:case-role";
    var folder = search.selectNodes(path)[0];
    for (var i in data) {
        var properties = [];
        properties['icaseRole:varName'] = data[i].name;
        properties['cm:title'] = data[i].title;
        properties['icaseRole:isReferenceRole'] = true;
        folder.createNode(null, "icaseRole:role", properties);
    }

}

createCaseRoles();
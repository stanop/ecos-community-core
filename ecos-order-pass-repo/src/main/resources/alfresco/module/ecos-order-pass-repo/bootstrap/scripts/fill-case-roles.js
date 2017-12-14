<import resource="classpath:alfresco/module/ecos-order-pass-repo/bootstrap/scripts/create-case-roles.js">

var data = [
    {name:"initiator", title:"\u0418\u043D\u0438\u0446\u0438\u0430\u0442\u043E\u0440"},
    {name:"owner", title:"\u041B\u0438\u043D\u0435\u0439\u043D\u044B\u0439 \u0440\u0443\u043A\u043E\u0432\u043E\u0434\u0438\u0442\u0435\u043B\u044C"},
    {name:"confirmer", title:"\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442"}

];

createCaseRoles();
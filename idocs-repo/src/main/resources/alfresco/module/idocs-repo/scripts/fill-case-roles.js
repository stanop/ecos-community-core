<import resource="classpath:alfresco/module/idocs-repo/scripts/import-nodes.lib.js">

var type = '{http://www.citeck.ru/model/icaseRole/1.0}role';
var destination = 'app:company_home/app:dictionary/cm:dataLists/cm:case-role';

var data = [
    { type: type, props: {
        'cm:name': 'author',
        'icaseRole:varName': 'author',
        'icaseRole:isReferenceRole': "true",
        'cm:title': {
            'en': 'Author',
            'ru': 'Автор'
        }
    }},
    { type: type, props: {
        'cm:name': 'process-owner',
        'icaseRole:varName': 'process-owner',
        'icaseRole:isReferenceRole': "true",
        'cm:title': {
            'en': 'Process owner',
            'ru': 'Владелец процесса'
        }
    }},
    { type: type, props: {
        'cm:name': 'process-reviewer',
        'icaseRole:varName': 'process-reviewer',
        'icaseRole:isReferenceRole': "true",
        'cm:title': {
            'en': 'Process reviewer',
            'ru': 'Эксперт процесса'
        }
    }}
];

importNodes(destination, data);

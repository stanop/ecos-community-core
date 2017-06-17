var pathToDictionary = "/app:company_home/app:dictionary";
var pathToDataLists = "/app:company_home/app:dictionary/cm:dataLists";

var folderDataLists = search.selectNodes(pathToDataLists)[0];

if (folderDataLists == null) {
    var folderDictionary = search.selectNodes(pathToDictionary)[0];
    if (folderDictionary != null) {
        folderDictionary.createNode('dataLists', 'cm:folder', {
            'cm:name' : 'dataLists'
        }, 'cm:contains', 'cm:dataLists');
    }
}

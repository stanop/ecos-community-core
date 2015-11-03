function replaceDocs(){
    var toDirName = archiveDirectory;
    var companyhome = search.selectNodes('/app:company_home')[0]
    var toDir = companyhome.childByNamePath(toDirName);
    if(toDir == null){
        toDir = companyhome.createFolder(toDirName);
    }
    var nodes = search.luceneSearch('PATH:"/app:company_home/st:sites//*" AND ASPECT:"dms:storageTime"')
    var today = new Date();
    for (var i in nodes){
        var document = nodes[i];
        var storagePeriod = document.properties['dms:storagePeriod'];
        var createDate = document.properties['cm:created']
          if((storagePeriod == '' || storagePeriod == null) && storagePeriod != 0){
              continue;
          }
        var storeUp = createDate.setMonth( createDate.getMonth() + storagePeriod);
        if(storeUp < today ){
            var dirName = document.getParent().properties["name"]
            var subDirInArchive = toDir.childByNamePath(dirName);
            if(subDirInArchive == null){
                subDirInArchive= toDir.createFolder(dirName);
            }
            document.move(subDirInArchive);
            document.removeAspect('dms:storageTime')
        }
    }

}
replaceDocs();
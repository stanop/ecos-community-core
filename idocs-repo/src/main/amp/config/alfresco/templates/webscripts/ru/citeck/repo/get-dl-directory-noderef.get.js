function main() {
    var type = args.dltype

    if (type != null) {
        var folderList = search.luceneSearch('TYPE:"dl:dataList" AND @dl\\:dataListItemType:"' + type + '"');
        if (folderList != null && folderList.length > 0) {
            model.data = folderList[0].nodeRef.toString()
            return;
        }
        model.data = 'null';
    }
}

main()
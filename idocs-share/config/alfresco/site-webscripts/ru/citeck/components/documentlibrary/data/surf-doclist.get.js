<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/data/surf-doclist.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/documentlibrary/data/surf-doclist-custom.lib.js">

function getFilterOptions() {
  var processOption = function(name) {
      return args[name] ? args[name] == "true" : false;
  };

  return {
      filter: args.filter ? args.filter : null
  };
}

var surfDoclist_main = function surfDoclist_main() {
   var json = "{}",
       options = getFilterOptions(),
       obj = {};

  if (options.filter && options.filter.length > 0) {
    var connector = remote.connect("alfresco"),
        result = connector.get("/citeck/search/simple?type=cm:cmobject&text=" + encodeURI(options.filter)),
        foundedNodes = eval("(" + result + ")").nodes;

    if (foundedNodes) {
      var nodeRefs = [];
      for (var i in foundedNodes) {
        if (foundedNodes[i].isContainer || foundedNodes[i].isDocument) {
          nodeRefs.push(foundedNodes[i].nodeRef);
        }
      }

      result = connector.post("/slingshot/doclib2/explicit/type/node/alfresco/user/home?filter=all", 
                              jsonUtils.toJSONString({ nodeRefs: nodeRefs.join(",") }), 
                              "application/json");
      obj = eval("(" + result + ")");
    }
  } else {
    var dataUrl = DocList_Custom.calculateRemoteDataURL(),
        result = remote.call(dataUrl);

    if (result.status == 200) {
      obj = eval('(' + result + ')');
    } else {
      status.setCode(result.status);
    }
  }

  if (obj && (obj.item || obj.items)) {
    DocList.processResult(obj, {
      actions: true,
      indicators: true,
      metadataTemplate: true
    });

    for (var i in obj.items) {
      if (obj.items[i].node.properties["cm:title"]) {
        obj.items[i].displayName = obj.items[i].node.properties["cm:title"];
      }
    }

    model.json = jsonUtils.toJSONString(obj);
  }
};

surfDoclist_main();
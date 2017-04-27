define(["dojo/_base/declare", "js/citeck/menus/citeckMenuItem"], function(declare, citeckMenuItem) {
  return declare([citeckMenuItem], {      
    cssRequirements: [ { cssFile:"../../alfresco/css/AlfMenuItem.css" } ]
  });
});
var app_share_existing = search.selectNodes("/sys:system/sys:zones/cm:APP.SHARE")[0];

if(!app_share_existing){
  var parent = search.selectNodes("/sys:system/sys:zones")[0];
  if(parent){
    var properties = new Array();
    properties['sys:node-uuid'] = 'APP.SHARE';
    var app_share_new = parent.createNode("APP.SHARE", "cm:zone", properties, "sys:children", "cm:APP.SHARE");
  }
}

define(["dojo/_base/declare", "dijit/MenuBarItem", "alfresco/menus/_AlfMenuItemMixin", "js/citeck/_citeck.lib",
        "alfresco/core/Core", "dojo/dom-construct", "dojo/dom-class"],
  function(declare, MenuBarItem, _AlfMenuItemMixin, _citecklib, AlfCore, domConstruct, domClass) {
    
    return declare([MenuBarItem, _AlfMenuItemMixin, AlfCore], {

      iconNode: null,

      postCreate: function alfresco_menus_AlfMenuBarPopup__postCreate() {
        if (this.label) {
            this.set("label", this.message(this.label));
        }

        if (this.movable) _citecklib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

        domClass.add(this.containerNode, "alf-menu-bar-label-node");
        if (this.iconClass && this.iconClass != "dijitNoIcon") {
            this.iconNode = domConstruct.create("img", {
                className: this.iconClass,
                src: Alfresco.constants.URL_RESCONTEXT + "/js/alfresco/menus/css/images/transparent-20.png",
                alt: this.message(this.iconAltText)
            }, this.focusNode, "first");

            if (this.label) {
                domClass.add(this.containerNode, this.labelWithIconClass);
            }
        }

        this.inherited(arguments);
      }
    });
});

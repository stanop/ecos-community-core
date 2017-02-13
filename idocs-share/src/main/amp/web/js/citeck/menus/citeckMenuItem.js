define(["dojo/_base/declare", "dijit/MenuItem", "js/citeck/_citeck.lib", 
        "alfresco/menus/_AlfMenuItemMixin", "alfresco/core/Core", "dojo/dom-class"],
        function(declare, MenuItem, _citecklib, _AlfMenuItemMixin, AlfCore, domClass) {

  return declare([MenuItem, _AlfMenuItemMixin, AlfCore], {

    clickEvent: null,
    inheriteClickEvent: false,

    movable: null,

    postMixInProperties: function alfresco_menus__AlfMenuItem__postMixInProperties() {
      if (this.clickEvent) this.clickEvent = eval("(" + this.clickEvent + ")")
      this.inherited(arguments);
    },

    onClick: function(event) {
      this.alfLog("log", "AlfMenuBarItem clicked");

      this.emitClosePopupEvent();
      event.stopPropagation();

      if (this.clickEvent) {
        this.clickEvent(event, document.getElementById(this.id), this);
        if (!this.inheriteClickEvent) return false;
      }

      if (this.targetUrl) {
        this.alfPublish("ALF_NAVIGATE_TO_PAGE", { url: this.targetUrl, type: this.targetUrlType, target: this.targetUrlLocation});
      } else if (this.publishTopic) {
        this.alfPublish(this.publishTopic, this.publishPayload ? this.publishPayload : {});
      } else {
        this.alfLog("error", "An AlfMenuItem was clicked but did not define a 'targetUrl' or 'publishTopic' or 'clickEvent' attribute", event);
      }
    },

    postCreate: function alfresco_menus_AlfMenuItem__postCreate() {
      if (this.movable) _citecklib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

      this.setupIconNode();
      this.inherited(arguments);
    }
  });
});

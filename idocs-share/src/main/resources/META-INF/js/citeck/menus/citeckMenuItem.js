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

      var self = this;
      var defaultHandler = function () {
        if (self.targetUrl) {
          self.alfPublish("ALF_NAVIGATE_TO_PAGE", { url: self.targetUrl, type: self.targetUrlType, target: self.targetUrlLocation});
        } else if (self.publishTopic) {
          self.alfPublish(self.publishTopic, self.publishPayload ? self.publishPayload : {});
        } else {
          self.alfLog("error", "An AlfMenuItem was clicked but did not define a 'targetUrl' or 'publishTopic' or 'clickEvent' attribute", event);
        }
      };

      if (this.actionType === "logout") {
        fetch("/eis.json", { credentials: 'include' })
          .then(function(r) { return r.json(); })
          .then(function(config) {
            var EIS_LOGOUT_URL_DEFAULT_VALUE = 'LOGOUT_URL';
            var logoutUrl = config.logoutUrl;

            if (logoutUrl === EIS_LOGOUT_URL_DEFAULT_VALUE) {
              return defaultHandler();
            }

            fetch(logoutUrl, {
              method: 'POST',
              mode: 'no-cors',
              credentials: 'include'
            }).then(() => {
              window.location.reload();
            });
          })
          .catch(function() {
            return defaultHandler();
          });
      } else {
        defaultHandler();
      }
    },

    postCreate: function alfresco_menus_AlfMenuItem__postCreate() {
      if (this.movable) _citecklib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

      this.setupIconNode();
      this.inherited(arguments);
    }
  });
});

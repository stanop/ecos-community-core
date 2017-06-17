define(["dojo/_base/declare", "dijit/MenuBarItem", "alfresco/menus/_AlfMenuItemMixin", "js/citeck/_citeck.lib",
        "alfresco/core/Core", "dojo/dom-construct", "dojo/dom-class"],
  function(declare, MenuBarItem, _AlfMenuItemMixin, _citecklib, AlfCore, domConstruct, domClass) {
    
    return declare([MenuBarItem, _AlfMenuItemMixin, AlfCore], {

        iconNode: null,
        iconImage: "/js/alfresco/menus/css/images/transparent-20.png",

        clickEvent: null,
        inheriteClickEvent: false,

        attributes: {},

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

        /**
         * Sets the label of the menu item that represents the popup and creates a new alfresco/menus/AlfMenuGroups
         * instance containing all of the widgets to be displayed in the popup. Ideally the array of widgets should
         * be instances of alfresco/menus/AlfMenuGroup (where instance has its own list of menu items). However, this
         * widget should be able to accommodate any widget.
         *
         * @instance
         */
        postCreate: function alfresco_menus_AlfMenuBarPopup__postCreate() {
            if (this.label) {
                this.set("label", this.message(this.label));
            }

            if (this.movable)
                _citecklib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

        domClass.add(this.containerNode, "alf-menu-bar-label-node");
        if (this.iconClass && this.iconClass != "dijitNoIcon") {
            this.iconNode = domConstruct.create("img", {
                className: this.iconClass,
                src: Alfresco.constants.URL_RESCONTEXT + this.iconImage,
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

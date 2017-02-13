define(["dojo/_base/declare", "dojo/text!../templates/citeckMenuGroup.html", "alfresco/core/Core",
        "alfresco/menus/AlfDropDownMenu", "alfresco/core/CoreRwd", "js/citeck/_citeck.lib",
        "dojo/_base/event", "dojo/dom-style", "dojo/dom-class", "dojo/keys", "dijit/popup", "dojo/string"],
        function(declare, template, AlfCore, AlfDropDownMenu, CoreRwd, _alflib, event, domStyle, domClass, keys, popup, string) {

  return declare([AlfDropDownMenu, AlfCore, CoreRwd], {

    templateString: template,

    cssRequirements: [{cssFile:"./css/AlfMenuGroup.css"}],

    label: "",

    constructor: function alfresco_menus_AlfMenuGroup__constructor(args) {
      this.templateString = string.substitute(template, { ddmTemplateString: AlfDropDownMenu.prototype.templateString});
    },


    postCreate: function alfresco_menus_AlfMenuGroup__postCreate() {
      if (this.label == "") {
        domStyle.set(this._groupTitleNode, "display", "none");
      } else {
        this.label = this.message(this.label);
        this._groupTitleNode.innerHTML = this.encodeHTML(this.label);
      }

      if (this.movable) _alflib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

      this.inherited(arguments);
    },

    isFocusable: function alfresco_menus_AlfMenuGroup__isFocusable() {
      return this.hasChildren();
    },

    _onRightArrow: function(/*Event*/ evt){
      if(this.focusedChild && this.focusedChild.popup && !this.focusedChild.disabled) {
        this.alfLog("log", "Open cascading menu");
        this._moveToPopup(evt);
      } else {
        this.alfLog("log", "Try and find a menu bar in the stack and move to next");
        var menuBarAncestor = this.findMenuBarAncestor(this.getParent());
        if (menuBarAncestor) {
          this.alfLog("log", "Go to next item in menu bar");
          menuBarAncestor.focusNext()
        }
      }
    },

    _onLeftArrow: function(evt) {
      if(this.getParent().parentMenu && !this.getParent().parentMenu._isMenuBar) {
        this.alfLog("log", "Close cascading menu");
        this.getParent().parentMenu.focusChild(this.getParent().parentMenu.focusedChild);
        popup.close(this.getParent());
      } else {
        var menuBarAncestor = this.findMenuBarAncestor(this.getParent());
        if (menuBarAncestor) {
          this.alfLog("log", "Focus previous item in menu bar");
          menuBarAncestor.focusPrev();
        } else {
          evt.stopPropagation();
          evt.preventDefault();
        }
      }
    },

    findMenuBarAncestor: function alfresco_menus_AlfMenuGroup__findMenuBarAncestor(currentMenu) {
      var reachedMenuTop = false;
      while (!reachedMenuTop && !currentMenu._isMenuBar) {
        if (currentMenu.parentMenu) {
          currentMenu = currentMenu.parentMenu;
        } else {
          var parent = currentMenu.getParent();
          while (parent && !parent.parentMenu) {
            parent = parent.getParent();
          }

          if (parent && parent.parentMenu) {
            currentMenu = parent.parentMenu;
          }
          reachedMenuTop = (parent == null);
        }
      }
      
      var menuBar = (currentMenu._isMenuBar) ? currentMenu : null;
      return menuBar;
    }
  });
});

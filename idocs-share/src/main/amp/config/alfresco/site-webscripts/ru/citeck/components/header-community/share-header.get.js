<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/header-community/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/header/header-tokens.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/citeck.lib.js">


// TODO:
// - fix click on open popup item


// GLOBAL VARIABLES
var isMobile = isMobileDevice(context.headers["user-agent"]);

// ---------------------
// HEADER MENU
// ---------------------

var header = getWidget("SHARE_HEADER"),
    appMenu = getWidget("HEADER_APP_MENU_BAR"),
    userMenu = getWidget("HEADER_USER_MENU_BAR"),
    search = getWidget("HEADER_SEARCH"),

    currentSite = page.url.templateArgs.site || getLastSite();

appMenu.config.id = "HEADER_APP_MENU_BAR";
userMenu.config.id = "HEADER_USER_MENU_BAR";

// SEARCH
header.config.widgets.splice(2, 1);
header.config.widgets.splice(1, 0, search);

// USER MENU ITEMS
var userMenuItems = [
  {
    id: "HEADER_USER_MENU_STATUS",
    name: "alfresco/header/CurrentUserStatus"
  },

  {
    id: "HEADER_USER_MENU_MY_PROFILE",
    name: "js/citeck/header/citeckMenuItem",
    config:
    {
      id: "HEADER_USER_MENU_MY_PROFILE",
      label: "header.my-profile.label",
      iconImage: "/share/res/components/images/header/my-profile.png",
      targetUrl: "user/" + encodeURIComponent(user.name) + "/profile"
    }
  }
];

var currentUser = user.getUser(user.id);
var availability = "make-" + (currentUser.properties.available === false ? "" : "not") + "available",
    clickEvent = function (event, element) {
        Citeck.forms.dialog("deputy:selfAbsenceEvent", "", {
            scope: this,
            fn: function (node) {
                this.alfPublish("ALF_NAVIGATE_TO_PAGE", {
                    url: this.targetUrl,
                    type: this.targetUrlType,
                    target: this.targetUrlLocation
                });
            }
        }, {
            title: "",
            destination: "workspace://SpacesStore/absence-events"
        })
    };

userMenuItems.push({
    id: "HEADER_USER_MENU_AVAILABILITY",
    name: "js/citeck/header/citeckMenuItem",
    config: {
        id: "HEADER_USER_MENU_AVAILABILITY",
        label: "header." + availability + ".label",
        iconImage: "/share/res/components/images/header/" + availability + ".png",
        targetUrl: "/components/deputy/make-available?available=" + (currentUser.properties.available === false ? "true" : "false"),
        clickEvent: "" + (currentUser.properties.available === false ? "" : clickEvent.toString())
    }
});

if (user.capabilities.isMutable) {
  userMenuItems.push({
    id: "HEADER_USER_MENU_PASSWORD",
    name: "js/citeck/header/citeckMenuItem",
    config:
    {
      id: "HEADER_USER_MENU_PASSWORD",
      label: "header.change-password.label",
      iconImage: "/share/res/components/images/header/change-password.png",
      targetUrl: "user/" + encodeURIComponent(user.name) + "/change-password"
    }
  });
}

if (!context.externalAuthentication) {
  userMenuItems.push({
    id: "HEADER_USER_MENU_LOGOUT",
    name: "js/citeck/header/citeckMenuItem",
    config:
    {
      id: "HEADER_USER_MENU_LOGOUT",
      label: "header.logout.label",
      iconImage: "/share/res/components/images/header/logout.png",
      publishTopic: "ALF_DOLOGOUT"
    }
  });
}

// USER MENU
userMenu.config.widgets = [{
  id: "HEADER_USER_MENU",
  name: "alfresco/header/AlfMenuBarPopup",
  config: {
    id: "HEADER_USER_MENU",
    label: user.fullName,
    widgets: [{
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        widgets: userMenuItems
      }
    }]
  }
}];


// APP MENU ITEMS
var createSiteClickEvent = function(event, element) {
  Alfresco.module.getCreateSiteInstance().show(); 
}
 
var HEADER_HOME = {
      id: "HEADER_HOME",
      name: "js/citeck/menus/citeckMenuBarItem",
      config: {
        id: "HEADER_HOME",
        label: "header.menu.home.label",
        targetUrl: "user/" + encodeURIComponent(user.name) + "/dashboard"
      }
    },
    HEADER_SITES_VARIANTS = {
      id: "HEADER_SITES_VARIANTS",
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        id: "HEADER_SITES_VARIANTS",
        widgets: buildSitesForUser(user.name)
      }
    },
    HEADER_SITES_SEARCH = {
      id: "HEADER_SITES_SEARCH",
      name: "js/citeck/menus/citeckMenuItem",
      config: {
        id: "HEADER_SITES_SEARCH",
        label: "header.find-sites.label",
        targetUrl: "site-finder"
      }
    },
    HEADER_SITES_CREATE = {
      id: "HEADER_SITES_CREATE",
      name: "js/citeck/menus/citeckMenuItem",
      config: {
        id: "HEADER_SITES_CREATE",
        label: "header.create-site.label",
        clickEvent: createSiteClickEvent.toString(),
        inheriteClickEvent: false
      }
    },
    HEADER_CREATE_VARIANTS = {
      id: "HEADER_CREATE_VARIANTS",
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        id: "HEADER_CREATE_VARIANTS",
        widgets: buildCreateVariantsForSite(currentSite)
      }
    },
    HEADER_JOURNALS = {
      id: "HEADER_JOURNALS",
      name: "js/citeck/menus/citeckMenuBarItem",
      config: {
        id: "HEADER_JOURNALS",
        label: "header.journals.label",
        targetUrl: buildSiteUrl(currentSite) + "journals2/list/main",
        movable: { minWidth: 1089 }
      }
    },
    HEADER_DOCUMENTLIBRARY = {
      id: "HEADER_DOCUMENTLIBRARY",
      name: "js/citeck/menus/citeckMenuBarItem",
      config: {
        id: "HEADER_DOCUMENTLIBRARY",
        label: "header.documentlibrary.label",
        targetUrl: buildSiteUrl(currentSite) + "documentlibrary",
        movable: { minWidth: 1171 }
      }
    },
    HEADER_CREATE_WORKFLOW_VARIANTS = {
      id: "HEADER_CREATE_WORKFLOW_VARIANTS",
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        id: "HEADER_CREATE_WORKFLOW_VARIANTS",
        widgets: [
          {
            id: "HEADER_CREATE_WORKFLOW_ADHOC",
            name: "js/citeck/menus/citeckMenuItem",
            config: {
              id: "HEADER_CREATE_WORKFLOW_ADHOC",
              label: "header.create-workflow-adhoc.label",
              targetUrl: "start-specified-workflow?workflowId=activiti$perform"
            }
          },
          {
            id: "HEADER_CREATE_WORKFLOW_CONFIRM",
            name: "js/citeck/menus/citeckMenuItem",
            config: {
              id: "HEADER_CREATE_WORKFLOW_CONFIRM",
              label: "header.create-workflow-confirm.label",
              targetUrl: "start-specified-workflow?workflowId=activiti$confirm"
            }
          }
        ]
      }
    };


// DEBUG MENU
var loggingWidgetItems;
if (config.global.flags.getChildValue("client-debug") == "true") {
  var loggingEnabled = false,
      allEnabled     = false,
      warnEnabled    = false,
      errorEnabled   = false;

  if (userPreferences &&
      userPreferences.org &&
      userPreferences.org.alfresco &&
      userPreferences.org.alfresco.share &&
      userPreferences.org.alfresco.share.logging) {

      var loggingPreferences = userPreferences.org.alfresco.share.logging;
      loggingEnabled = loggingPreferences.enabled && true;
      allEnabled = (loggingPreferences.all != null) ?  loggingPreferences.all : false;
      warnEnabled = (loggingPreferences.warn != null) ?  loggingPreferences.warn : false;
      errorEnabled = (loggingPreferences.error != null) ?  loggingPreferences.error : false;
   }

  loggingWidgetItems = [
    {
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        label: "Quick Settings",
        widgets: [
          {
            name: "alfresco/menus/AlfCheckableMenuItem",
            config: {
              label: "Debug Logging",
              value: "enabled",
              publishTopic: "ALF_LOGGING_STATUS_CHANGE",
              checked: loggingEnabled
            }
          },
          {
            name: "alfresco/menus/AlfCheckableMenuItem",
            config: {
              label: "Show All Logs",
              value: "all",
              publishTopic: "ALF_LOGGING_STATUS_CHANGE",
              checked: allEnabled
            }
          },
          {
            name: "alfresco/menus/AlfCheckableMenuItem",
            config: {
              label: "Show Warning Messages",
              value: "warn",
              publishTopic: "ALF_LOGGING_STATUS_CHANGE",
              checked: warnEnabled
            }
          },
          {
            name: "alfresco/menus/AlfCheckableMenuItem",
            config: {
              label: "Show Error Messages",
              value: "error",
              publishTopic: "ALF_LOGGING_STATUS_CHANGE",
              checked: errorEnabled
            }
          },
        ]
      }
    },
    {
      name: "js/citeck/menus/citeckMenuGroup",
      config: {
        label: "Logging Configuration",
        widgets: [
          {
            name: "js/citeck/menus/citeckMenuItem",
            config: {
              label: "Update Logging Preferences",
              publishTopic: "ALF_UPDATE_LOGGING_PREFERENCES"
            }
          }
        ]
      }
    }
  ];
}


// BUILD MAIN MENU
appMenu.config.widgets = [];

// BUILD DESKTOP MENU
if (!isMobile) {
  appMenu.config.widgets.push(HEADER_HOME);
  appMenu.config.widgets.push({
    id: "HEADER_SITES",
    name: "alfresco/header/AlfMenuBarPopup",
    config: {
      id: "HEADER_SITES",
      label: "header.sites.label",
      widgets: [
        HEADER_SITES_VARIANTS,
        {
          id: "HEADER_SITES_MANAGEMENT",
          name: "js/citeck/menus/citeckMenuGroup",
          config: {
            id: "HEADER_SITES_MANAGEMENT",
            widgets: [ 
              HEADER_SITES_SEARCH, 
              HEADER_SITES_CREATE 
            ]
          }
        }
      ]
    }
  }); 
  appMenu.config.widgets.push({
    id: "HEADER_CREATE",
    name: "alfresco/header/AlfMenuBarPopup",
    config: {
      id: "HEADER_CREATE",
      label: "header.create-variants.label",
      widgets: [ HEADER_CREATE_VARIANTS ]
    }
  })
  appMenu.config.widgets.push(HEADER_JOURNALS);
  appMenu.config.widgets.push(HEADER_DOCUMENTLIBRARY);
  appMenu.config.widgets.push({
    id: "HEADER_CREATE_WORKFLOW",
    name: "alfresco/header/AlfMenuBarPopup",
    config: {
      id: "HEADER_CREATE_WORKFLOW",
      label: "header.create-workflow.label",
      widgets: [ HEADER_CREATE_WORKFLOW_VARIANTS ]
    }
  });

  appMenu.config.widgets.push(buildMorePopup(false));

  if (loggingWidgetItems) {
    appMenu.config.widgets.push({
      id: "HEADER_LOGGING",
      name: "alfresco/header/AlfMenuBarPopup",
      config: {
        id: "HEADER_LOGGING",
        label: "Debug Menu",
        widgets: loggingWidgetItems
      }
    });
  };
}

// BUILD MOBILE MENU
var HEADER_MOBILE_HOME = toMobileWidget(HEADER_HOME);
HEADER_MOBILE_HOME.name = "js/citeck/menus/citeckMenuItem";

var HEADER_MOBILE_JOURNALS = toMobileWidget(HEADER_JOURNALS);
HEADER_MOBILE_JOURNALS.name = "js/citeck/menus/citeckMenuItem";
HEADER_MOBILE_JOURNALS.config.movable = null;

var HEADER_MOBILE_DOCUMENTLIBRARY = toMobileWidget(HEADER_DOCUMENTLIBRARY);
HEADER_MOBILE_DOCUMENTLIBRARY.name = "js/citeck/menus/citeckMenuItem";
HEADER_MOBILE_DOCUMENTLIBRARY.config.movable = null;

var HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS = toMobileWidget(HEADER_CREATE_WORKFLOW_VARIANTS);
HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS.config.label = "header.create-workflow.label";

var HEADER_MOBILE_CREATE_VARIANTS = toMobileWidget(HEADER_CREATE_VARIANTS);
HEADER_MOBILE_CREATE_VARIANTS.config.label = "header.create-variants.label";

var HEADER_MOBILE_SITES_VARIANTS = toMobileWidget(HEADER_SITES_VARIANTS);
var HEADER_MOBILE_SITES_SEARCH = toMobileWidget(HEADER_SITES_SEARCH);
var HEADER_MOBILE_SITES_CREATE = toMobileWidget(HEADER_SITES_CREATE);

var HEADER_MOBILE_MENU_VARIANTS = {
  id: "HEADER_MOBILE_MENU_VARIANTS",
  name: "js/citeck/menus/citeckMenuGroup",
  config: {
    id: "HEADER_MOBILE_MENU_VARIANTS",
    widgets: [
      HEADER_MOBILE_HOME,
      HEADER_MOBILE_JOURNALS,
      HEADER_MOBILE_DOCUMENTLIBRARY,

      {
        id: "HEADER_MOBILE_SITES",
        name: "js/citeck/menus/citeckMenuGroup",
        config: {
          id: "HEADER_MOBILE_SITES",
          label: "header.sites.label",
          widgets: [
            HEADER_MOBILE_SITES_VARIANTS, 
            {
              id: "HEADER_MOBILE_SITES_MANAGEMENT",
              name: "js/citeck/menus/citeckMenuGroup",
              config: {
                id: "HEADER_MOBILE_SITES_MANAGEMENT",
                widgets: [ HEADER_MOBILE_SITES_SEARCH, HEADER_MOBILE_SITES_CREATE ]
              }
            }
          ]
        }
      },

      HEADER_MOBILE_CREATE_VARIANTS,
      HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS
    ]
  }
};

HEADER_MOBILE_MENU_VARIANTS.config.widgets.push(buildMorePopup(true));

if (loggingWidgetItems) {
  HEADER_MOBILE_MENU_VARIANTS.config.widgets.push({
    id: "HEADER_LOGGING",
    name: "alfresco/header/AlfMenuGroup",
    config: {
      id: "HEADER_MOBILE_LOGGING",
      label: "Debug Menu",
      widgets: loggingWidgetItems
    }
  });
}

appMenu.config.widgets.unshift({
   id: "HEADER_MOBILE_MENU",
   name: "alfresco/header/AlfMenuBarPopup",
   config: {
      id: "HEADER_MOBILE_MENU",
      widgets: [ HEADER_MOBILE_MENU_VARIANTS ]
   }
});

// ---------------------
// TITLE MENU
// ---------------------

var siteConfig = getWidget("HEADER_SITE_CONFIGURATION_DROPDOWN"),
    siteData = getSiteData();

if (siteConfig && siteData.userIsSiteManager) {
  if (!page.titleId && !hasWidget("HEADER_CUSTOMIZE_SITE_DASHBOARD")) {
    siteConfig.config.widgets.splice(0, 0, {
      id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
      name: "js/citeck/menus/citeckMenuItem",
      config: {
        label: "customize_dashboard.label",
        iconClass: "alf-cog-icon",
        targetUrl: "site/" + page.url.templateArgs.site + "/customise-site-dashboard"
      }
    });
  }
}


// ---------------------
// FUNCTIONS
// ---------------------

function buildMorePopup(isMobile) {
  var config = {
    widgets: [ buildMyGroup(isMobile) ]
  };

  if (!isMobile) {
    config.label = "header.more.label";
    config.widgets.unshift(buildMovableGroup());
  }

  if (user.isAdmin) config.widgets.push(buildAdminGroup(isMobile));

  return {
    id: "HEADER_MORE",
    name: isMobile ? "js/citeck/menus/citeckMenuGroup" : "alfresco/header/AlfMenuBarPopup",
    config: config
  };
};

function buildMovableGroup() {
  return {
    id: "HEADER_MORE_MOVABLE_GROUP",
    name: "js/citeck/menus/citeckMenuGroup",
    config: {
      id: "HEADER_MORE_MOVABLE_GROUP",
      label: "",
      movable: { maxWidth: 1170 },
      widgets: buildItems([
        {
          id: "journals",
          url: buildSiteUrl(currentSite) + "journals2/list/main",
          movable: { maxWidth: 1080 }
        },
        {
          id: "documentlibrary", url: buildSiteUrl(currentSite) + "documentlibrary",
          movable: { maxWidth: 1170 }
        }
      ], "movable")
    }
  };
};

function buildMyGroup(isMobile) {
  var id = "HEADER_" + (isMobile ? "MOBILE" : "") + "_MORE_MY_GROUP",
      group = (isMobile ? "MOBILE_" : "") + "MORE";

  return {
    id: id,
    name: "js/citeck/menus/citeckMenuGroup",
    config: {
      id: id,
      label: "header.my.label",
      widgets: buildItems([
        { id: "task-journals", url: "journals2/list/tasks", iconImage: "/share/res/components/images/header/my-tasks.png" },
        { id: "my-workflows", url: "my-workflows", iconImage: "/share/res/components/images/header/my-workflows.png" },
        { id: "completed-workflows", url: "completed-workflows#paging=%7C&filter=workflows%7Call", iconImage: "/share/res/components/images/header/completed-workflows.png" },
        { id: "my-content", url: "user/user-content", iconImage: "/share/res/components/images/header/my-content.png" },
        { id: "my-sites", url: "user/user-sites", iconImage: "/share/res/components/images/header/my-sites.png" },
        { id: "my-profile", url: "user/" + encodeURIComponent(user.name) + "/profile", iconImage: "/share/res/components/images/header/my-profile.png" },
        { id: "my-files", url: "context/mine/myfiles", iconImage: "/share/res/components/images/header/my-content.png" },
        { id: "global_journals2", url: "journals2/list/main", iconImage: "/share/res/components/images/header/journals.png" }
      ], group)
    }
  };
};

function buildAdminGroup(isMobile) {
  var id = "HEADER_" + (isMobile ? "MOBILE" : "") + "_MORE_TOOLS_GROUP",
      group = (isMobile ? "MOBILE_" : "") + "MORE_TOOLS";

  return {
    id: id,
    name: "js/citeck/menus/citeckMenuGroup",
    config: {
      id: id,
      label: "header.tools.label",
      widgets: buildItems([
        { id: "repository", url: "repository", iconImage: "/share/res/components/images/header/repository.png" },
        { id: "application", url: "console/admin-console/application", iconImage: "/share/res/components/images/header/application.png" },
        { id: "groups", url: "console/admin-console/groups", iconImage: "/share/res/components/images/header/groups.png" },
        { id: "users", url: "console/admin-console/users", iconImage: "/share/res/components/images/header/users.png" },
        { id: "categories", url: "console/admin-console/type-manager", iconImage: "/share/res/components/images/header/category-manager.png" },
        { id: "system", url: "journals2/list/system", iconImage: "/share/res/components/images/header/journals.png" },
        { id: "meta_journals", url: "journals2/list/meta", iconImage: "/share/res/components/images/header/journals.png" },
        { id: "templates", url: "journals2/list/templates", iconImage: "/share/res/components/images/header/templates.png" },
        { id: "orgstruct", url: "orgstruct", iconImage: "/share/res/components/images/header/orgstruct.png" },
        { id: "more", url: "console/admin-console/", iconImage: "/share/res/components/images/header/more.png" }
      ], group)
    }
  };
};

function buildSiteUrl(sitename) {
  return sitename ? "site/" + encodeURIComponent(sitename) + "/" : "";
};

function buildItems(items, groupName) {
  var result = [];

  for (var i = 0; i < items.length; i++) {
    var id = "HEADER_" + (groupName + "_" + items[i].id.replace(/-/, "_")).toUpperCase(),
        configuration = {
          id: id,
          label: items[i].label || "header." + items[i].id + ".label",
          targetUrl: items[i].url,
          targetUrlType: items[i].urlType || "SHARE_PAGE_RELATIVE"
        };

    if (items[i].iconImage) configuration["iconImage"] = items[i].iconImage;
    if (items[i].movable) configuration["movable"] = items[i].movable;

    result.push({
      id: configuration.id,
      name: "js/citeck/menus/citeckMenuItem",
      config: configuration
    });
  }

  return result;
};

function buildSitesForUser(username) {
  var sitesPresets = [],
      result = remote.call("/api/people/" + encodeURIComponent(username) + "/sites");

  if (result.status == 200 && result != "{}") {
    var sites = eval('(' + result + ')');

    if (sites && sites.length > 0) {
      for (var sd = 0; sd < sites.length; sd++) {
        sitesPresets.push({
          url: "?site=" + sites[sd].shortName,
          id: sites[sd].shortName.replace(/-/, "_"),
          label: sites[sd].title
        });
      }
    }
  }

  return buildItems(sitesPresets, "SITE");
};

function buildCreateVariantsForSite(sitename) {
  var createVariantsPresets = [],
      result = remote.call("/api/journals/create-variants/site/" + encodeURIComponent(sitename));

  if (result.status == 200 && result != "{}") {
    var responseData = eval('(' + result + ')'),
        createVariants = responseData.createVariants;

    if (createVariants && createVariants.length > 0) {
      for (var cv = 0; cv < createVariants.length; cv++) {
        createVariantsPresets.push({
          label: createVariants[cv].title,
          id: createVariants[cv].type.replace(":", "_"),
          url: "node-create?type=" + createVariants[cv].type + "&viewId=" + createVariants[cv].formId + "&destination=" + createVariants[cv].destination
        });
      }
    }
  }

  return buildItems(createVariantsPresets, "CREATE_VARIANT");
}

function toMobileWidget(object) {
  var widget = jsonUtils.toObject(jsonUtils.toJSONObject(object));
  widget.id = widget.config.id = widget.id.replace("HEADER_", "HEADER_MOBILE_");

  if (widget.config.widgets && widget.config.widgets.length > 0) {
    for (var i in widget.config.widgets) {
      widget.config.widgets[i] = toMobileWidget(widget.config.widgets[i])
    }
  }
  
  return widget;
}
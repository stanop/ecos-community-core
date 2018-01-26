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

var header = findObjectById(model.jsonModel.widgets, "SHARE_HEADER"),
    appMenu = findObjectById(model.jsonModel.widgets, "HEADER_APP_MENU_BAR"),
    userMenu = findObjectById(model.jsonModel.widgets, "HEADER_USER_MENU_BAR"),
    search = findObjectById(model.jsonModel.widgets, "HEADER_SEARCH"),
    shareVerticalLayout = findObjectById(model.jsonModel.widgets, "SHARE_VERTICAL_LAYOUT"),
    titleMenu = findObjectById(model.jsonModel.widgets, "HEADER_TITLE_MENU"),
    navigationMenu = findObjectById(model.jsonModel.widgets, "HEADER_NAVIGATION_MENU_BAR"),
    customizeUserDashboard = findObjectById(model.jsonModel.widgets, "HEADER_CUSTOMIZE_USER_DASHBOARD"),
    currentSite = page.url.templateArgs.site || getLastSiteFromCookie();

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

userMenuItems.push({
    id: "HEADER_USER_MENU_FEEDBACK",
    name: "js/citeck/menus/citeckMenuItem",
    config: {
        id: "HEADER_USER_MENU_FEEDBACK",
        label: "header.feedback.label",
        iconImage: "/share/res/components/images/header/default-error-report-16.png",
        targetUrl: "https://www.citeck.ru/feedback",
        targetUrlType: "FULL_PATH",
        targetUrlLocation: "NEW"
    }
});

userMenuItems.push({
    id: "HEADER_USER_MENU_REPORTISSUE",
    name: "js/citeck/menus/citeckMenuItem",
    config: {
        id: "HEADER_USER_MENU_REPORTISSUE",
        label: "header.reportIssue.label",
        iconImage: "/share/res/components/images/header/default-feedback-16.png",
        targetUrl: "mailto:support@citeck.ru?subject=Ошибка в работе Citeck EcoS: краткое описание&body=Summary: Короткое описание проблемы (продублировать в теме письма)%0A%0ADescription:%0AПожалуйста, детально опишите возникшую проблему, последовательность действий, которая привела к ней. При необходимости приложите скриншоты.",
        targetUrlType: "FULL_PATH",
        targetUrlLocation: "NEW"
    }
});

if (!context.externalAuthentication) {
    var logoutItemConfig = {
        id: "HEADER_USER_MENU_LOGOUT",
        name: "js/citeck/header/citeckMenuItem",
        config: {
              id: "HEADER_USER_MENU_LOGOUT",
              label: "header.logout.label",
              iconImage: "/share/res/components/images/header/logout.png"
        }
    };

    // Alfresco community version doesn't have LogoutService so we should check
    if (model.jsonModel.services && model.jsonModel.services.indexOf("alfresco/services/LogoutService") > -1) {
        logoutItemConfig.config.publishTopic = "ALF_DOLOGOUT";
    } else {
        logoutItemConfig.config.targetUrl = "dologout";
    }
    userMenuItems.push(logoutItemConfig);
}

// USER MENU
userMenu.config.widgets = [
    {
        id: "HEADER_USER_MENU",
        name: "alfresco/header/AlfMenuBarPopup",
        config: {
            id: "HEADER_USER_MENU",
            label: user.fullName,
            style: isMobile ? "padding-left: 5px;" : "padding-left: 10px;",
            widgets: [{
                name: "js/citeck/menus/citeckMenuGroup",
                config: {
                    widgets: userMenuItems
                }
            }]
        }
    }
];

// delete the Title Bar everywhere (exept for Edit Page and Create Page)
if (shareVerticalLayout && shareVerticalLayout.config.widgets.length) {
    shareVerticalLayout.config.widgets = shareVerticalLayout.config.widgets.filter(function(item) {
        if (item.id == "HEADER_TITLE_BAR" && (page.id.indexOf("edit") != -1 || page.id.indexOf("create") != -1 || page.id.indexOf("start") != -1)) {
            item.config.widgets = item.config.widgets.filter(function(item) {
                return item.id == "HEADER_TITLE"
            });
            return item;
        } else {
            return item.id !== "HEADER_TITLE_BAR"
        }

    })
}

if (customizeUserDashboard) {
    customizeUserDashboard.name = "js/citeck/header/citeckMenuItem";
    customizeUserDashboard.config.label = msg.get("customize_dashboard.label");
}
var siteMenuItems = [];
if (navigationMenu && navigationMenu.config.widgets.length) {
    for (var w in navigationMenu.config.widgets) {
        if (navigationMenu.config.widgets[w].id == "HEADER_SITE_MORE_PAGES" && navigationMenu.config.widgets[w].config.widgets.length) {
            navigationMenu.config.widgets = navigationMenu.config.widgets.concat(
                navigationMenu.config.widgets[w].config.widgets[0].config.widgets.map(function(widget) {
                    widget.name = "js/citeck/header/citeckMenuItem";
                    return widget;
                }));
            navigationMenu.config.widgets.splice(w, 1);
        }
    }
    navigationMenu.config.widgets = navigationMenu.config.widgets.filter(function(item) {
        item.name = "alfresco/header/AlfMenuItem";
        return user.isAdmin || item.id != "HEADER_SITE_SITE-DOCUMENT-TYPES";
    });
    siteMenuItems = siteMenuItems.concat(navigationMenu.config.widgets);
}

if (titleMenu && titleMenu.config.widgets.length) {
    titleMenu.config.widgets = titleMenu.config.widgets.map(function(item) {
        if(item.id == "HEADER_SITE_CONFIGURATION_DROPDOWN") {
            item.name = "js/citeck/menus/citeckMenuGroup";
            item.config.label = msg.get("header.menu.siteConfig.altText");
            if (item.config.widgets.length) {
                item.config.widgets = item.config.widgets.map(function(widget) {
                    widget.name = "js/citeck/header/citeckMenuItem";
                    return widget;
                })
            }

        }
        if(item.id == "HEADER_SITE_INVITE") {
            item.name = "js/citeck/header/citeckMenuItem";
            item.config.label = msg.get("header.menu.invite.altText");
        }
        return item;
    });
    siteMenuItems = siteMenuItems.concat(titleMenu.config.widgets);
}

if (siteMenuItems.length) {
    userMenu.config.widgets.unshift({
        id: "HEADER_SITE_MENU",
        name: "alfresco/header/AlfMenuBarPopup",
        config: {
            id: "HEADER_SITE_MENU",
            label: "",
            style: isMobile ? "padding-left: 5px; padding-right: 5px" :  "padding-left: 10px; padding-right: 10px",
            widgets: siteMenuItems
        }
    })
}

// APP MENU ITEMS
var createSiteClickEvent = "Citeck.module.getCreateSiteInstance().show()";
 
// var HEADER_SITES_VARIANTS = {
//       id: "HEADER_SITES_VARIANTS",
//       name: "js/citeck/menus/citeckMenuGroup",
//       config: {
//         id: "HEADER_SITES_VARIANTS",
//         widgets: buildSitesForUser(user.name)
//       }
//     },
//    var HEADER_SITES_SEARCH = {
//       id: "HEADER_SITES_SEARCH",
//       name: "js/citeck/menus/citeckMenuItem",
//       config: {
//         id: "HEADER_SITES_SEARCH",
//         label: "header.find-sites.label",
//         targetUrl: "site-finder"
//       }
//     },
//     HEADER_SITES_CREATE = {
//       id: "HEADER_SITES_CREATE",
//       name: "js/citeck/menus/citeckMenuItem",
//       config: {
//         id: "HEADER_SITES_CREATE",
//         label: "header.create-site.label",
//         clickEvent: createSiteClickEvent.toString(),
//         inheriteClickEvent: false
//       }
//     },
    // HEADER_CREATE_VARIANTS = {
    //   id: "HEADER_CREATE_VARIANTS",
    //   name: "js/citeck/menus/citeckMenuGroup",
    //   config: {
    //     id: "HEADER_CREATE_VARIANTS",
    //     widgets: buildCreateVariantsForSite(currentSite)
    //   }
    // },

var HEADER_DOCUMENTLIBRARY = {
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
            widgets: [{
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
            }]
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
          }
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
  // appMenu.config.widgets.push({
  //   id: "HEADER_SITES",
  //   name: "alfresco/header/AlfMenuBarPopup",
  //   config: {
  //     id: "HEADER_SITES",
  //     label: "header.sites.label",
  //     widgets: [
  //       HEADER_SITES_VARIANTS,
  //       {
  //         id: "HEADER_SITES_MANAGEMENT",
  //         name: "js/citeck/menus/citeckMenuGroup",
  //         config: {
  //           id: "HEADER_SITES_MANAGEMENT",
  //           widgets: [
  //             HEADER_SITES_SEARCH,
  //             HEADER_SITES_CREATE
  //           ]
  //         }
  //       }
  //     ]
  //   }
  // });
  // appMenu.config.widgets.push({
  //   id: "HEADER_CREATE",
  //   name: "alfresco/header/AlfMenuBarPopup",
  //   config: {
  //     id: "HEADER_CREATE",
  //     label: "header.create-variants.label",
  //     widgets: [ HEADER_CREATE_VARIANTS ]
  //   }
  // });
  //appMenu.config.widgets.push(HEADER_JOURNALS);
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
  }
}

// BUILD MOBILE MENU
// var HEADER_MOBILE_JOURNALS = toMobileWidget(HEADER_JOURNALS);
// HEADER_MOBILE_JOURNALS.name = "js/citeck/menus/citeckMenuItem";
// HEADER_MOBILE_JOURNALS.config.movable = null;
//
// var HEADER_MOBILE_DOCUMENTLIBRARY = toMobileWidget(HEADER_DOCUMENTLIBRARY);
// HEADER_MOBILE_DOCUMENTLIBRARY.name = "js/citeck/menus/citeckMenuItem";
// HEADER_MOBILE_DOCUMENTLIBRARY.config.movable = null;
//
// var HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS = toMobileWidget(HEADER_CREATE_WORKFLOW_VARIANTS);
// HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS.config.label = "header.create-workflow.label";

// var HEADER_MOBILE_CREATE_VARIANTS = toMobileWidget(HEADER_CREATE_VARIANTS);
// HEADER_MOBILE_CREATE_VARIANTS.config.label = "header.create-variants.label";
//
// var HEADER_MOBILE_SITES_VARIANTS = toMobileWidget(HEADER_SITES_VARIANTS);
// var HEADER_MOBILE_SITES_SEARCH = toMobileWidget(HEADER_SITES_SEARCH);
// var HEADER_MOBILE_SITES_CREATE = toMobileWidget(HEADER_SITES_CREATE);

// var HEADER_MOBILE_MENU_VARIANTS = {
//   id: "HEADER_MOBILE_MENU_VARIANTS",
//   name: "js/citeck/menus/citeckMenuGroup",
//   config: {
//     id: "HEADER_MOBILE_MENU_VARIANTS",
//     widgets: [
//       HEADER_MOBILE_JOURNALS,
//       HEADER_MOBILE_DOCUMENTLIBRARY,
//       {
//         id: "HEADER_MOBILE_SITES",
//         name: "js/citeck/menus/citeckMenuGroup",
//         config: {
//           id: "HEADER_MOBILE_SITES",
//           label: "header.sites.label",
//           widgets: [
//             HEADER_MOBILE_SITES_VARIANTS,
//             {
//               id: "HEADER_MOBILE_SITES_MANAGEMENT",
//               name: "js/citeck/menus/citeckMenuGroup",
//               config: {
//                 id: "HEADER_MOBILE_SITES_MANAGEMENT",
//                 widgets: [ HEADER_MOBILE_SITES_SEARCH, HEADER_MOBILE_SITES_CREATE ]
//               }
//             }
//           ]
//         }
//       },
//
//       HEADER_MOBILE_CREATE_VARIANTS,
//       HEADER_MOBILE_CREATE_WORKFLOW_VARIANTS
//     ]
//   }
// };

// HEADER_MOBILE_MENU_VARIANTS.config.widgets.push(buildMorePopup(true));
//
// if (loggingWidgetItems) {
//   HEADER_MOBILE_MENU_VARIANTS.config.widgets.push({
//     id: "HEADER_LOGGING",
//     name: "js/citeck/header/citeckMenuGroup",
//     config: {
//       id: "HEADER_MOBILE_LOGGING",
//       label: "Debug Menu",
//       widgets: loggingWidgetItems
//     }
//   });
// }

appMenu.config.widgets.unshift({
    id: "HEADER_SLIDE_MENU",
    name: "js/citeck/header/citeckMainSlideMenu",
    config: {
        id: "HEADER_SLIDE_MENU",
        isMobile: isMobile,
        userName: user.name,
        logoSrc: getHeaderLogoUrl(),
        logoSrcMobile: url.context + "/res/themes/" + theme + "/images/app-logo-mobile.png",
        widgets: getWidgets(isMobile ? true : false)
    }
});
// appMenu.config.widgets.unshift(
//     {
//         id: "HEADER_MOBILE_MENU",
//         name: "alfresco/header/AlfMenuBarPopup",
//         config: {
//             id: "HEADER_MOBILE_MENU",
//             widgets: [HEADER_MOBILE_MENU_VARIANTS],
//             style: "padding-right: 5px;"
//         }
//     },
//    buildLogo(isMobile)
// );

// ---------------------
// TITLE MENU
// ---------------------

var siteConfig = findObjectById(model.jsonModel.widgets, "HEADER_SITE_CONFIGURATION_DROPDOWN"),
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
        widgets: [buildMyGroup(isMobile)]
    };

    if (!isMobile) {
        config.label = "header.more.label";
        config.widgets.unshift(buildMovableGroup());
    }

    if (user.isAdmin) config.widgets.push(buildAdminGroup(isMobile));

    return {
        id: "HEADER_" + (isMobile ? "MOBILE_" : "") + "MORE",
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
  var id = "HEADER_" + (isMobile ? "MOBILE_" : "") + "MORE_MY_GROUP",
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
        { id: "global_journals2", url: "journals2/list/main", iconImage: "/share/res/components/images/header/journals.png" },
        { id: "orgstruct", url: "orgstruct", iconImage: "/share/res/components/images/header/orgstruct.png" }
      ], group)
    }
  };
};

function buildAdminGroup(isMobile) {
  var id = "HEADER_" + (isMobile ? "MOBILE_" : "") + "MORE_TOOLS_GROUP",
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
        { id: "more", url: "console/admin-console/", iconImage: "/share/res/components/images/header/more.png" }
      ], group)
    }
  };
};

function buildSitesForUser(username) {
    var sitesPresets = [], result = remote.call("/api/people/" + encodeURIComponent(username) + "/sites");

    if (result.status == 200 && result != "{}") {
        var sites = eval('(' + result + ')');

        if (sites && sites.length > 0) {
            for (var sd = 0; sd < sites.length; sd++) {
                sitesPresets.push({
                    id: sites[sd].shortName,
                    url: "/share/page/site/" + sites[sd].shortName + "/dashboard",
                    label: sites[sd].title,
                    widgets: buildJournalsListForSite(sites[sd].shortName)
                });
            }
        }
    }
    sitesPresets.push({
            id: "HEADER_SITES_SEARCH",
            label: "header.find-sites.label",
            url: "/share/page/site-finder"
        },
        {
            id: "HEADER_SITES_CREATE",
            label: "header.create-site.label",
            clickEvent: createSiteClickEvent
        });

    return sitesPresets;
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
          id: createVariants[cv].type,
          url: "/share/page/node-create?type=" + createVariants[cv].type + "&viewId=" + createVariants[cv].formId + "&destination=" + createVariants[cv].destination
        });
      }
    }
  }

  return createVariantsPresets;
}

function buildJournalsListForSite(sitename) {
    var journalsResult = [],
        result = remote.call("/api/journals/list?journalsList=site-" + encodeURIComponent(sitename) + "-main");

    if (result.status == 200 && result != "{}") {
        var responseData = eval('(' + result + ')'),
            journals = responseData.journals;

        if (journals && journals.length) {
            for (var j = 0; j < journals.length; j++) {
                var url = "/share/page/site/" + sitename + "/journals2/list/main#journal=" + journals[j].nodeRef;
                journalsResult.push({
                    label: journals[j].title,
                    id: journals[j].type + "-journal",
                    url: url + "&filter=",
                    widgets: buildFiltersForJournal(journals[j].type, url)
                });
            }
        }
    }

    return journalsResult;
}

function buildFiltersForJournal(journalType, journalUrl) {
    var filtersResult = [],
        result = remote.call("/api/journals/filters?journalType=" + journalType);

    if (result.status == 200 && result != "{}") {
        var responseData = eval('(' + result + ')'),
            filters = responseData.filters;

        if (filters && filters.length) {
            for (var f = 0; f < filters.length; f++) {
                filtersResult.push({
                    label: filters[f].title,
                    id: filters[f].type + "-filter",
                    url: journalUrl + "&filter=" + filters[f].nodeRef
                });
            }
        }
    }

    return filtersResult;
}

// function buildLogo(isMobile) {
//     return {
//         id: isMobile ? "HEADER_MOBILE_LOGO" : "HEADER_LOGO",
//         name: "js/citeck/logo/citeckLogo",
//         config: {
//             id: isMobile ? "HEADER_MOBILE_LOGO" : "HEADER_LOGO",
//             logoClasses: "alfresco-logo-only",
//             currentTheme: theme,
//             logoSrc: isMobile ? url.context + "/res/themes/" + theme + "/images/app-logo-mobile.png" : getHeaderLogoUrl(),
//             targetUrl: "user/" + encodeURIComponent(user.name) + "/dashboard"
//         }
//     };
// };

function getWidgets(isMobile) {
    // TODO: Add request
    var mainMenu = [{
            id: "HEADER_MENU_SECTION_1",
            sectionTitle: "header.section-1.label",
            widgets: [
                {
                    id: "HEADER_MENU_JOURNAL_OF_CONTRACTS",
                    label: "header.journal-of-contracts.label",
                    widgets: [
                        {id: "HEADER_MENU_JOURNAL_OF_CONTRACTS_FILTER_1", label: "header.filter-1.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"},
                        {id: "HEADER_MENU_JOURNAL_OF_CONTRACTS_FILTER_2", label: "header.filter-2.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"}
                    ]
                }
            ]
        },
            {
                id: "HEADER_MENU_SECTION_2",
                sectionTitle: "header.section-2.label",
                widgets: [
                    {
                        id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS",
                        label: "header.journal-of-attorneys.label",
                        widgets: [
                            {id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS_FILTER_1", label: "header.filter-1.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"},
                            {id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS_FILTER_2", label: "header.filter-2.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"}
                        ]
                    }
                ]
            },
            {
                id: "HEADER_MENU_SITES",
                sectionTitle: "header.sites.label",
                widgets: buildSitesForUser(user.name)
            },
            {
                id: "HEADER_MENU_ORGSTRUCT",
                sectionTitle: "header.orgstruct.label",
                widgets: [{id: "HEADER_MENU_ORGSTRUCT_WIDGET", label: "header.orgstruct.label", url: "/share/page/orgstruct"}]
            }
        ],
        mobileMenu = [{
                id: "HEADER_MENU_SECTION_1",
                sectionTitle: "header.section-1.label",
                widgets: [
                    {
                        id: "HEADER_MENU_JOURNAL_OF_CONTRACTS",
                        label: "header.journal-of-contracts.label",
                        widgets: [
                            {id: "HEADER_MENU_JOURNAL_OF_CONTRACTS_FILTER_1", label: "header.filter-1.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"},
                            {id: "HEADER_MENU_JOURNAL_OF_CONTRACTS_FILTER_2", label: "header.filter-2.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"}
                        ]
                    }
                ]
            },
            {
                id: "HEADER_MENU_SECTION_2",
                sectionTitle: "header.section-2.label",
                widgets: [
                    {
                        id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS",
                        label: "header.journal-of-attorneys.label",
                        widgets: [
                            {id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS_FILTER_1", label: "header.filter-1.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"},
                            {id: "HEADER_MENU_JOURNAL_OF_ATTORNEYS_FILTER_2", label: "header.filter-2.label", url: "/share/page/site/contracts/journals2/list/main#journal=workspace%3A%2F%2FSpacesStore%2F35761250-1f9c-4ba5-ba42-1f7ff3d2f402&filter=&settings=&skipCount=0&maxItems=10"}
                        ]
                    }
                ]
            },
            {
                id: "HEADER_MENU_SITES",
                sectionTitle: "header.sites.label",
                widgets: buildSitesForUser(user.name)
            },
            {
                id: "HEADER_MENU_ORGSTRUCT",
                sectionTitle: "header.orgstruct.label",
                widgets: [{id: "HEADER_MENU_ORGSTRUCT_WIDGET", label: "header.orgstruct.label", url: "/share/page/orgstruct"}]
            },
            {
                id: "HEADER_DOCUMENTLIBRARY",
                sectionTitle: "header.documentlibrary.label",
                widgets: [{id: "HEADER_DOCUMENTLIBRARY_WIDGET", label: "header.documentlibrary.label", url: "/share/page/" + buildSiteUrl(currentSite) + "documentlibrary"}]
            },
            {
                id: "HEADER_CREATE_WORKFLOW",
                sectionTitle: "header.create-workflow.label",
                widgets: [{
                        id: "HEADER_CREATE_WORKFLOW_ADHOC",
                        label: "header.create-workflow-adhoc.label",
                        url: "/share/page/start-specified-workflow?workflowId=activiti$perform"
                    },
                    {
                        id: "HEADER_CREATE_WORKFLOW_CONFIRM",
                        label: "header.create-workflow-confirm.label",
                        url: "/share/page/start-specified-workflow?workflowId=activiti$confirm"
                    }]
            },
            {
                id: "HEADER_MORE_MY_GROUP",
                sectionTitle: "header.my.label",
                widgets: [
                    { id: "task-journals", url: "/share/page/journals2/list/tasks", label: "header.task-journals.label" },
                    { id: "my-workflows", url: "/share/page/my-workflows", label: "header.my-workflows.label" },
                    { id: "completed-workflows", url: "/share/page/completed-workflows#paging=%7C&filter=workflows%7Call", label: "header.completed-workflows.label" },
                    { id: "my-content", url: "/share/page/user/user-content", label: "header.my-content.label" },
                    { id: "my-sites", url: "/share/page/user/user-sites", label: "header.my-sites.label" },
                    { id: "my-profile", url: "/share/page/user/" + encodeURIComponent(user.name) + "/profile", label: "header.my-profile.label" },
                    { id: "my-files", url: "/share/page/context/mine/myfiles", label: "header.my-files.label" },
                    { id: "global_journals2", url: "/share/page/journals2/list/main", label: "header.global_journals2.label" }
                ]
            },
            {
                id: "HEADER_MORE_TOOLS_GROUP",
                sectionTitle: "header.tools.label",
                widgets: [
                    { id: "repository", url: "/share/page/repository", label: "header.repository.label" },
                    { id: "application", url: "/share/page/console/admin-console/application", label: "header.application.label" },
                    { id: "groups", url: "/share/page/console/admin-console/groups", label: "header.groups.label" },
                    { id: "users", url: "/share/page/console/admin-console/users", label: "header.users.label" },
                    { id: "categories", url: "/share/page/console/admin-console/type-manager", label: "header.categories.label" },
                    { id: "system", url: "/share/page/journals2/list/system", label: "header.system.label" },
                    { id: "meta_journals", url: "/share/page/journals2/list/meta", label: "header.meta_journals.label" },
                    { id: "templates", url: "/share/page/journals2/list/templates", label: "header.templates.label" },
                    { id: "more", url: "/share/page/console/admin-console/", label: "header.more.label" }
                ]
            }

        ];

    return isMobile ? mobileMenu : mainMenu;
};


model.__alf_current_site__ = currentSite;
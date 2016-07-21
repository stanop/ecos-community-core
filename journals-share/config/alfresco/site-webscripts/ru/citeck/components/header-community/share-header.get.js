<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

//------------
// HEADER MENU
//------------

var appMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR"),
    userMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU_BAR"),
    currentSite = page.url.templateArgs.site || "";

// USER MENU
var userMenuItems = [
  {
     id: "HEADER_USER_MENU_STATUS",
     name: "alfresco/header/CurrentUserStatus",
     config: { id: "HEADER_USER_MENU_STATUS" }
  },

  {
     id: "HEADER_USER_MENU_MY_PROFILE",
     name: "alfresco/header/AlfMenuItem",
     config:
     {
        id: "HEADER_USER_MENU_MY_PROFILE",
        label: "header.my-profile.label",
        iconImage: "/share/res/components/images/header/my-profile.png",
        targetUrl: "user/" + encodeURIComponent(user.name) + "/profile"
     }
  }
];

if (user.properties.available === true || user.properties.available === null) {
  userMenuItems.push({
     id: "HEADER_USER_MENU_MAKE_NOTAVAILABLE",
     name: "alfresco/header/AlfMenuItem",
     config:
     {
        id: "HEADER_USER_MENU_MAKE_NOTAVAILABLE",
        label: "header.make-notavailable.label",
        iconImage: "/share/res/components/images/header/make-notavailable.png",
        targetUrl: "/components/delegate/make-available?available=false"
     }
  });
} else if (user.properties.available === false) {
  userMenuItems.push({
     id: "HEADER_USER_MENU_MAKE_AVAILABLE",
     name: "alfresco/header/AlfMenuItem",
     config:
     {
        id: "HEADER_USER_MENU_MAKE_AVAILABLE",
        label: "header.make-available.label",
        iconImage: "/share/res/components/images/header/make-available.png",
        targetUrl: "/components/delegate/make-available?available=true"
     }
  });
}

if (user.capabilities.isMutable) {
   userMenuItems.push({
      id: "HEADER_USER_MENU_PASSWORD",
      name: "alfresco/header/AlfMenuItem",
      config:
      {
         id: "HEADER_USER_MENU_CHANGE_PASSWORD",
         label: "header.change-password.label",
         iconImage: "/share/res/components/images/header/change-password.png",
         targetUrl: "user/" + encodeURIComponent(user.name) + "/change-password"
      }
   });
}

if (!context.externalAuthentication) {
   userMenuItems.push({
      id: "HEADER_USER_MENU_LOGOUT",
      name: "alfresco/header/AlfMenuItem",
      config:
      {
         id: "HEADER_USER_MENU_LOGOUT",
         label: "header.logout.label",
         iconImage: "/share/res/components/images/header/logout.png",
         targetUrl: "dologout"
      }
   });
}

userMenu.config.widgets = [
  {
     id: "HEADER_USER_MENU_POPUP",
     name: "alfresco/header/AlfMenuBarPopup",
     config: {
        id: "HEADER_USER_MENU_POPUP",
        label: user.fullName,
        widgets: [
           {
              id: "HEADER_USER_MENU",
              name: "alfresco/menus/AlfMenuGroup",
              config: {
                 id: "HEADER_USER_MENU",
                 widgets: userMenuItems
              }
           }
        ]
     }
  }
];


// APP MENU
appMenu.config.widgets = [
  {
     id: "HEADER_HOME",
     name: "alfresco/menus/AlfMenuBarItem",
     config: {
        id: "HEADER_HOME",
        label: "header.menu.home.label",
        targetUrl: "user/" + encodeURIComponent(user.name) + "/dashboard",
        targetUrlType: "SHARE_PAGE_RELATIVE"
     }
  },

  {
     id: "HEADER_SITES_MENU",
     name: "alfresco/header/AlfSitesMenu",
     config: {
        id: "HEADER_SITES_MENU",
        label: "header.menu.sites.label",
        currentSite: currentSite,
        currentUser: user.name
     }
  },

  // {
  //    id: "HEADER_CREATE_VARIANTS",
  //    name: "modules/header/create-variants",
  //    config: {
  //       id: "HEADER_CREATE_VARIANTS",
  //       label: "header.create-variants.label",
  //       currentSite: currentSite,
  //       currentUser: user.name
  //    }
  // },

  {
     id: "HEADER_JOURNALS",
     name: "alfresco/menus/AlfMenuBarItem",
     config: {
        id: "HEADER_JOURNALS",
        iconImage: "journals.png",
        label: "header.journals.label",
        targetUrl: buildSiteUrl(currentSite) + "journals2/list/main"
     }
  },

  {
     id: "HEADER_DOCUMENTLIBRARY",
     name: "alfresco/menus/AlfMenuBarItem",
     config: {
        id: "HEADER_DOCUMENTLIBRARY",
        label: "header.documentlibrary.label",
        targetUrl: buildSiteUrl(currentSite) + "documentlibrary"
     }
  },

  {
     id: "HEADER_CREATE_WORKFLOW",
     name: "alfresco/header/AlfMenuBarPopup",
     config: {
        id: "HEADER_CREATE_WORKFLOW",
        label: "header.create-workflow.label",
        widgets: [
          {
            id: "HEADER_CREATE_WORKFLOW_VARIANTS",
            name: "alfresco/menus/AlfMenuGroup",
            config: {
              widgets: [
                {
                   id: "HEADER_CREATE_WORKFLOW_ADHOC",
                   name: "alfresco/menus/AlfMenuItem",
                   config: {
                      id: "HEADER_CREATE_WORKFLOW_ADHOC",
                      label: "header.create-workflow-adhoc.label",
                      targetUrl: "start-specified-workflow?workflowId=activiti$perform"
                   }
                },
                {
                   id: "HEADER_CREATE_WORKFLOW_CONFIRM",
                   name: "alfresco/menus/AlfMenuItem",
                   config: {
                      id: "HEADER_CREATE_WORKFLOW_CONFIRM",
                      label: "header.create-workflow-confirm.label",
                      targetUrl: "start-specified-workflow?workflowId=activiti$confirm"
                   }
                }
              ]
            }
          }
        ]
     }
  },

  buildMorePopup()
];

// DEBUG MENU
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

   var loggingWidget = {
      name: "alfresco/header/AlfMenuBarPopup",
      config: {
         label: "Debug Menu",
         widgets: [
            {
               name: "alfresco/menus/AlfMenuGroup",
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
               name: "alfresco/menus/AlfMenuGroup",
               config: {
                  label: "Logging Configuration",
                  widgets: [
                     {
                        name: "alfresco/menus/AlfMenuItem",
                        config: {
                           label: "Update Logging Preferences",
                           publishTopic: "ALF_UPDATE_LOGGING_PREFERENCES"
                        }
                     }
                  ]
               }
            }
         ]
      }
   };

   appMenu.config.widgets.push(loggingWidget);
}




function buildMorePopup() {
  var groups = [ buildMyGroup() ];
  if (user.isAdmin) groups.push(buildAdminGroup());

  return {
     id: "HEADER_MORE",
     name: "alfresco/header/AlfMenuBarPopup",
     config: {
        id: "HEADER_MORE",
        label: "header.more.label",
        widgets: groups
     }
  }
};

function buildMyGroup() {
  return {
    id: "HEADER_MORE_MY_GROUP",
    name: "alfresco/menus/AlfMenuGroup",
    config: {
      id: "HEADER_MORE_MY_GROUP",
      label: "header.my.label",
      widgets: buildItems([
        { id: "task-journals", url: "journals2/list/tasks", iconImage: "/share/res/components/images/header/my-tasks.png" },
        { id: "my-workflows", url: "my-workflows", iconImage: "/share/res/components/images/header/my-workflows.png" },
        { id: "completed-workflows", url: "completed-workflows#paging=%7C&filter=workflows%7Call", iconImage: "/share/res/components/images/header/completed-workflows.png" },
        { id: "my-content", url: "user/user-content", iconImage: "/share/res/components/images/header/my-content.png" },
        { id: "my-sites", url: "user/user-sites", iconImage: "/share/res/components/images/header/my-sites.png" },
        { id: "my-profile", url: "user/admin/profile", iconImage: "/share/res/components/images/header/my-profile.png" },
        { id: "my-files", url: "context/mine/myfiles", iconImage: "/share/res/components/images/header/my-content.png" },
        { id: "global_journals2", url: "journals2/list/main", iconImage: "/share/res/components/images/header/journals.png" }
      ])
    }
  };
};

function buildAdminGroup() {
  return {
    id: "HEADER_MORE_TOOLS_GROUP",
    name: "alfresco/menus/AlfMenuGroup",
    config: {
      id: "HEADER_MORE_TOOLS_GROUP",
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
      ])
    }
  };
};

function buildSiteUrl(sitename) {
  return sitename ? "site/" + encodeURIComponent(sitename) : "";
};

function buildItems(items, groupName) {
  var result = [];
  for (var i = 0; i < items.length; i++) {
    result.push({
      id: ("HEADER_MORE_" + groupName + "_" + items[i].id).toUpperCase(),
      name: "alfresco/menus/AlfMenuItem",
      config: {
         id: ("HEADER_MORE_" + groupName + "_" + items[i].id).toUpperCase(),
         label: "header." + items[i].id + ".label",
         targetUrl: items[i].url,
         iconImage: items[i].iconImage
      }
    });
  }

  return result;
};

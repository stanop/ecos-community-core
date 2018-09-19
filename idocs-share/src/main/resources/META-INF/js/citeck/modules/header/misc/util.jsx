export function t(message) {
    if (!message) {
        return '';
    }

    if (!window.Alfresco) {
        return message;
    }

    return window.Alfresco.util.message(message);
}

export function makeSiteMenuItems(user, siteData) {
    let siteMenuItems = [];

    // If the user is an admin, and a site member, but NOT the site manager then
    // add the menu item to let them become a site manager...
    if (user.isAdmin && siteData.userIsMember && !siteData.userIsSiteManager) {
        siteMenuItems.push({
            id: "HEADER_BECOME_SITE_MANAGER",
            name: "alfresco/menus/AlfMenuItem",
            config: {
                id: "HEADER_BECOME_SITE_MANAGER",
                label: "become_site_manager.label",
                // iconClass: "alf-cog-icon",
                publishTopic: "ALF_BECOME_SITE_MANAGER",
                publishPayload: {
                    site: siteData.id,
                    siteTitle: siteData.profile.title,
                    user: user.name,
                    userFullName: user.fullName,
                    reloadPage: true
                }
            }
        });
    }

    // If the user is a site manager then let them make custmomizations...
    if (siteData.userIsSiteManager) {

        // Add Customize Dashboard
        siteMenuItems.push({
            id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
            name: "alfresco/menus/AlfMenuItem",
            config: {
                id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
                label: "customize_dashboard.label",
                // iconClass: "alf-cog-icon",
                targetUrl: "/share/page/site/" + siteData.id + "/customise-site-dashboard"
            }
        });

        // Add the regular site manager options (edit site, customize site, leave site)
        siteMenuItems.push(
            {
                id: "HEADER_EDIT_SITE_DETAILS",
                name: "alfresco/menus/AlfMenuItem",
                config: {
                    id: "HEADER_EDIT_SITE_DETAILS",
                    label: "edit_site_details.label",
                    // iconClass: "alf-edit-icon",
                    publishTopic: "ALF_EDIT_SITE",
                    publishPayload: {
                        site: siteData.id,
                        siteTitle: siteData.profile.title,
                        user: user.name,
                        userFullName: user.fullName
                    }
                }
            },
            {
                id: "HEADER_CUSTOMIZE_SITE",
                name: "alfresco/menus/AlfMenuItem",
                config: {
                    id: "HEADER_CUSTOMIZE_SITE",
                    label: "customize_site.label",
                    // iconClass: "alf-cog-icon",
                    targetUrl: "/share/page/site/" + siteData.id + "/customise-site"
                }
            },
            {
                id: "HEADER_LEAVE_SITE",
                name: "alfresco/menus/AlfMenuItem",
                config: {
                    id: "HEADER_LEAVE_SITE",
                    label: "leave_site.label",
                    // iconClass: "alf-leave-icon",
                    publishTopic: "ALF_LEAVE_SITE",
                    publishPayload: {
                        site: siteData.id,
                        siteTitle: siteData.profile.title,
                        user: user.name,
                        userFullName: user.fullName
                    }
                }
            }
        );
    } else if (siteData.userIsMember) {
        // If the user is a member of a site then give them the option to leave...
        siteMenuItems.push({
            id: "HEADER_LEAVE_SITE",
            name: "alfresco/menus/AlfMenuItem",
            config: {
                id: "HEADER_LEAVE_SITE",
                label: "leave_site.label",
                // iconClass: "alf-leave-icon",
                publishTopic: "ALF_LEAVE_SITE",
                publishPayload: {
                    site: siteData.id,
                    siteTitle: siteData.profile.title,
                    user: user.name,
                    userFullName: user.fullName
                }
            }
        });
    } else if (siteData.profile.visibility !== "PRIVATE" || user.isAdmin) {
        // If the member is not a member of a site then give them the option to join...
        siteMenuItems.push({
            id: "HEADER_JOIN_SITE",
            name: "alfresco/menus/AlfMenuItem",
            config: {
                id: "HEADER_JOIN_SITE",
                label: (siteData.profile.visibility === "MODERATED" ? "join_site_moderated.label" : "join_site.label"),
                // iconClass: "alf-leave-icon",
                publishTopic: (siteData.profile.visibility === "MODERATED" ? "ALF_REQUEST_SITE_MEMBERSHIP" : "ALF_JOIN_SITE"),
                publishPayload: {
                    site: siteData.id,
                    siteTitle: siteData.profile.title,
                    user: user.name,
                    userFullName: user.fullName
                }
            }
        });
    }

    return siteMenuItems;
}

export const makeUserMenuItems = (userName, isAvailable) => {
    const availability = "make-" + (isAvailable === false ? "" : "not") + "available";

    let clickEvent = function (event, element) {
        event.preventDefault();

        Citeck.forms.dialog("deputy:selfAbsenceEvent", "", {
            scope: this,
            fn: function (node) {
                // TODO!!!!!!!
                // this.alfPublish("ALF_NAVIGATE_TO_PAGE", {
                //     url: this.targetUrl,
                //     type: this.targetUrlType,
                //     target: this.targetUrlLocation
                // });
            }
        }, {
            title: "",
            destination: "workspace://SpacesStore/absence-events"
        })
    };

    return [
        {
            id: "HEADER_USER_MENU_MY_PROFILE",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.my-profile.label",
            iconClass: "fa-user",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/profile"
        },
        {
            id: "HEADER_USER_MENU_AVAILABILITY",
            name: "js/citeck/header/citeckMenuItem",
            label: "header." + availability + ".label",
            iconClass: "fa-user-times", // TODO
            targetUrl: "/share/page/components/deputy/make-available?available=" + (isAvailable === false ? "true" : "false"),
            clickEvent: "" + (isAvailable === false ? "" : clickEvent.toString())
        },
        {
            id: "HEADER_USER_MENU_PASSWORD",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.change-password.label",
            iconClass: "fa-key",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/change-password"
        },
        {
            id: "HEADER_USER_MENU_FEEDBACK",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.feedback.label",
            iconClass: "fa-exclamation-circle",
            targetUrl: "https://www.citeck.ru/feedback",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_REPORTISSUE",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.reportIssue.label",
            iconClass: "fa-comment",
            targetUrl: "mailto:support@citeck.ru?subject=Ошибка в работе Citeck EcoS: краткое описание&body=Summary: Короткое описание проблемы (продублировать в теме письма)%0A%0ADescription:%0AПожалуйста, детально опишите возникшую проблему, последовательность действий, которая привела к ней. При необходимости приложите скриншоты.",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_LOGOUT",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.logout.label",
            iconClass: "fa-times-circle"
        }
    ];
};

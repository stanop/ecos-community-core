export function t(messageId, scope = '') {
    // https://dev.alfresco.com/resource/docs/aikau-jsdoc/Core.js.html
    if (!messageId) {
        return '';
    }

    if (!window.Alfresco) {
        return messageId;
    }

    if (scope) {
        return window.Alfresco.util.message(messageId, scope);
    }

    let msg = messageId;
    // Check the global message bundle for the message id (this will get overridden if a more specific property is available)...
    if (typeof window.Alfresco.messages.global === "object") {
        const globalMsg = window.Alfresco.messages.global[messageId];
        if (typeof globalMsg === "string") {
            msg = globalMsg;
        }
    }

    // Overwrite with page scope...
    if (typeof window.Alfresco.messages.pageScope === "object") {
        const pageScopeMsg = window.Alfresco.messages.pageScope[messageId];
        if (typeof pageScopeMsg === "string") {
            msg = pageScopeMsg;
        }
    }

    // Overwrite page scope with default scope...
    if (typeof window.Alfresco.messages.scope[window.Alfresco.messages.defaultScope] === "object") {
        const scopeMsg = window.Alfresco.messages.scope[window.Alfresco.messages.defaultScope][messageId];
        if (typeof scopeMsg === "string") {
            msg = scopeMsg;
        }
    }

    return msg;
}

export function makeSiteMenuItems(user, siteData) {
    let siteMenuItems = [];

    if (!(!user.isAdmin && siteData.profile.visibility !== "PUBLIC" && siteData.userIsMember === false)) {
        // siteMenuItems = getSiteNavigationWidgets();
        siteMenuItems = [
            {
                id: "HEADER_SITE_DASHBOARD",
                label: "page.siteDashboard.title",
                targetUrl: "/share/page/site/" + siteData.id + "/dashboard",
            }
        ];

        let pages = [
            { pageId: 'documentlibrary', title: 'Каталог', pageUrl: 'documentlibrary', },
            { pageId: 'wiki-page', title: 'Журналы', pageUrl: 'journals2/list/main', },
            { pageId: 'blog-postlist', title: 'Управление типами кейсов на сайте', pageUrl: 'site-document-types', },
        ];

        for (var i=0; i < pages.length; i++) {
            var targetUrl = "/share/page/site/" + siteData.id + "/" + pages[i].pageUrl;
            siteMenuItems.push({
                id: "HEADER_SITE_" + pages[i].pageId.toUpperCase(),
                label: (pages[i].sitePageTitle) ? pages[i].sitePageTitle : pages[i].title,
                targetUrl: targetUrl,
            });
        }

        siteMenuItems.push({
            id: "HEADER_SITE_MEMBERS",
            label: "page.siteMembers.title",
            targetUrl: "/share/page/site/" + siteData.id + "/site-members"
        });
    }

    // If the user is an admin, and a site member, but NOT the site manager then
    // add the menu item to let them become a site manager...
    if (user.isAdmin && siteData.userIsMember && !siteData.userIsSiteManager) {
        siteMenuItems.push({
            id: "HEADER_BECOME_SITE_MANAGER",
            label: "become_site_manager.label",
            control: {
                type: "ALF_BECOME_SITE_MANAGER",
                payload: {
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
            label: "customize_dashboard.label",
            targetUrl: "/share/page/site/" + siteData.id + "/customise-site-dashboard"
        });

        // Add the regular site manager options (edit site, customize site, leave site)
        siteMenuItems.push(
            {
                id: "HEADER_EDIT_SITE_DETAILS",
                label: "edit_site_details.label",
                control: {
                    type: "ALF_EDIT_SITE",
                    payload: {
                        site: siteData.id,
                        siteTitle: siteData.profile.title,
                        user: user.name,
                        userFullName: user.fullName
                    }
                }
            },
            {
                id: "HEADER_CUSTOMIZE_SITE",
                label: "customize_site.label",
                targetUrl: "/share/page/site/" + siteData.id + "/customise-site"
            },
            {
                id: "HEADER_LEAVE_SITE",
                label: "leave_site.label",
                control: {
                    type: "ALF_LEAVE_SITE",
                    payload: {
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
            label: "leave_site.label",
            control: {
                type: "ALF_LEAVE_SITE",
                payload: {
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
            label: (siteData.profile.visibility === "MODERATED" ? "join_site_moderated.label" : "join_site.label"),
            control: {
                type: (siteData.profile.visibility === "MODERATED" ? "ALF_REQUEST_SITE_MEMBERSHIP" : "ALF_JOIN_SITE"),
                payload: {
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

    return [
        {
            id: "HEADER_USER_MENU_MY_PROFILE",
            label: "header.my-profile.label",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/profile"
        },
        {
            id: "HEADER_USER_MENU_AVAILABILITY",
            label: "header." + availability + ".label",
            targetUrl: "/share/page/components/deputy/make-available?available=" + (isAvailable === false ? "true" : "false"),
            control: isAvailable === false ? null : {
                type: "ALF_SHOW_MODAL_MAKE_UNAVAILABLE",
                payload: {
                    targetUrl: "/share/page/components/deputy/make-available?available=" + (isAvailable === false ? "true" : "false"),
                }
            }
        },
        {
            id: "HEADER_USER_MENU_PASSWORD",
            label: "header.change-password.label",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/change-password"
        },
        {
            id: "HEADER_USER_MENU_FEEDBACK",
            label: "header.feedback.label",
            targetUrl: "https://www.citeck.ru/feedback",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_REPORTISSUE",
            label: "header.reportIssue.label",
            targetUrl: "mailto:support@citeck.ru?subject=Ошибка в работе Citeck EcoS: краткое описание&body=Summary: Короткое описание проблемы (продублировать в теме письма)%0A%0ADescription:%0AПожалуйста, детально опишите возникшую проблему, последовательность действий, которая привела к ней. При необходимости приложите скриншоты.",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_LOGOUT",
            label: "header.logout.label",
            control: {
                type: "ALF_DOLOGOUT"
            }
        }
    ];
};

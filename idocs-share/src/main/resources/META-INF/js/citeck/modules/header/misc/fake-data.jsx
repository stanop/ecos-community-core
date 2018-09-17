let clickEvent = function (event, element) {
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

export const siteMenuItems = [
    {
        id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
        targetUrl: "#", // "site/" + page.url.templateArgs.site + "/customise-site-dashboard"
        label: "Главная страница сайта"
    },
    {
        label: "Библиотека документов"
    },
    {
        label: "Участники сайта"
    },
    {
        id: "HEADER_LEAVE_SITE",
        targetUrl: "#",
        label: "Покинуть сайт", // leave_site.label
        icon: "fa-sign-out"
    }
];

export const getUserMenuItems = (userName) => {
    return [
        {
            id: "HEADER_USER_MENU_MY_PROFILE",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.my-profile.label",
            icon: "fa-user",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/profile"
        },
        {
            id: "HEADER_USER_MENU_AVAILABILITY",
            name: "js/citeck/header/citeckMenuItem",
            label: "header." + "make-available.label", // TODO "header." + availability + ".label"
            icon: "fa-user-times",
            targetUrl: "/share/page/components/deputy/make-available?available=false",
            clickEvent: clickEvent.toString()
        },
        {
            id: "HEADER_USER_MENU_PASSWORD",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.change-password.label",
            icon: "fa-key",
            targetUrl: "/share/page/user/" + encodeURIComponent(userName) + "/change-password"
        },
        {
            id: "HEADER_USER_MENU_FEEDBACK",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.feedback.label",
            icon: "fa-exclamation-circle",
            targetUrl: "https://www.citeck.ru/feedback",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_REPORTISSUE",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.reportIssue.label",
            icon: "fa-comment",
            targetUrl: "mailto:support@citeck.ru?subject=Ошибка в работе Citeck EcoS: краткое описание&body=Summary: Короткое описание проблемы (продублировать в теме письма)%0A%0ADescription:%0AПожалуйста, детально опишите возникшую проблему, последовательность действий, которая привела к ней. При необходимости приложите скриншоты.",
            targetUrlType: "FULL_PATH",
            target: "_blank"
        },
        {
            id: "HEADER_USER_MENU_LOGOUT",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.logout.label",
            icon: "fa-times-circle"
        }
    ];
};

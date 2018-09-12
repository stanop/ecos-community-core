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
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashboard", label:"customize_dashboard.label", image:"/share/res/themes/citeckTheme/images/settings.png"},
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashboard", label:"customize_dashboard", image:"/share/res/themes/citeckTheme/images/settings.png"},
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashboard", label:"header.my-profile.label", icon:"fa-address-card"},
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashboard", label:"dqqeeqedd"},
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashboard", label:"dddddddd dddddddddd dddddddddd", icon:"fa-spin"},
    {id:"HEADER_CUSTOMIZE_USER_DASHBOARD", targetUrl: "/share/page/customise-user-dashoard", label:"fffff dddddddddd dddddddddd", icon:"fa-spinner"}
];

export const getUserMenuItems = (userName) => {
    return [
        {
            id: "HEADER_USER_MENU_MY_PROFILE",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.my-profile.label",
            image: "/share/res/components/images/header/my-profile.png",
            targetUrl: "share/page/user/" + encodeURIComponent(userName) + "/profile"
        },
        {
            id: "HEADER_USER_MENU_AVAILABILITY",
            name: "js/citeck/header/citeckMenuItem",
            label: "header." + "make-available.label",
            image: "/share/res/components/images/header/" + "make-available.png",
            targetUrl: "components/deputy/make-available?available=false",
            clickEvent: clickEvent.toString()
        },
        {
            id: "HEADER_USER_MENU_PASSWORD",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.change-password.label",
            image: "/share/res/components/images/header/change-password.png",
            targetUrl: "user/" + encodeURIComponent(userName) + "/change-password"
        },
        {
            id: "HEADER_USER_MENU_FEEDBACK",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.feedback.label",
            image: "/share/res/components/images/header/default-error-report-16.png",
            targetUrl: "https://www.citeck.ru/feedback",
            targetUrlType: "FULL_PATH",
            targetUrlLocation: "NEW"
        },
        {
            id: "HEADER_USER_MENU_REPORTISSUE",
            name: "js/citeck/menus/citeckMenuItem",
            label: "header.reportIssue.label",
            image: "/share/res/components/images/header/default-feedback-16.png",
            targetUrl: "mailto:support@citeck.ru?subject=Ошибка в работе Citeck EcoS: краткое описание&body=Summary: Короткое описание проблемы (продублировать в теме письма)%0A%0ADescription:%0AПожалуйста, детально опишите возникшую проблему, последовательность действий, которая привела к ней. При необходимости приложите скриншоты.",
            targetUrlType: "FULL_PATH",
            targetUrlLocation: "NEW"
        },
        {
            id: "HEADER_USER_MENU_LOGOUT",
            name: "js/citeck/header/citeckMenuItem",
            label: "header.logout.label",
            image: "/share/res/components/images/header/logout.png"
        }
    ];
};
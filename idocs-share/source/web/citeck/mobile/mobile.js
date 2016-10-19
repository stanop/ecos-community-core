//  PUBLIC METHODS
if (!Citeck) var Citeck = {};
Citeck.mobile = Citeck.mobile || {};

Citeck.mobile.hasTouchEvent = function() {
    try {
        document.createEvent('TouchEvent');
        return true;
    } catch(e) { return false; }
};

Citeck.mobile.isMobileDevice = function() {
    var ua = (navigator.userAgent || navigator.vendor || window.opera);
    if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(ua) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(ua.substr(0, 4))) {
        return true;
    } else { return false; }
};


// WORK PROCESS
(function() {

    if (Citeck.mobile.isMobileDevice()) {       
        YAHOO.Bubbling.on("on-mobile-device", function(e, args) { 
            if (args.fn) fn();
        });

        $(document).ready(function() {
            // global mobile style
            mobileGlobalClassToggle(true)

            // viewport only for dashboard (while development process)
            // and for all pages in production after all tests
            transformDashboard(true);
            transformJournalsSidebar(true);
            transformForm(true);
        });
    } else {
        $(document).ready(function() {
            var mobileWidth = (function() {
                var defaultMobileWidth = 525, themesMobileWidth = {
                        "yui-skin-citeckTheme": 585,
                        "yui-skin-uedmsTheme": 543
                    };

                var bodyThemeClass = document.getElementById("Share").className.match(/yui-skin-\w+Theme/),
                    yuiThemeName = bodyThemeClass ? bodyThemeClass[0] : "";

                return themesMobileWidth[yuiThemeName] || defaultMobileWidth;
            })();

            $("#bd .grid").attr("data-class-backup", $("#bd .grid").attr("class"));

            $(window).resize(function(event) {
                var functions = [ 
                        mobileGlobalClassToggle, 
                        transformJournalsSidebar, 
                        transformDashboard,
                        transformForm
                    ],
                    isMobile = window.innerWidth <= mobileWidth;
                for (var f in functions) { functions[f](isMobile); }
            });

            $(window).resize();
        });
    }


    function mobileGlobalClassToggle(isMobile) {
        isMobile ? $("body").addClass("mobile") : $("body").removeClass("mobile");
    }

    function viewportToggle(isMobile) {
        if (isMobile) {
            $("head").append($("<meta>", { name: "viewport", content: "width=device-width, initial-scale=1.0" }));
        } else { $("meta[name='viweport']").remove(); }
    }


    function transformJournalsSidebar(isMobile) {
        if (window.location.pathname.indexOf("journals2/list") != -1) {
            viewportToggle(isMobile);
            if (isMobile) {
                $("#alfresco-journals #alf-filters")
                    .hide()
                    .on("click.sidebar-toggle", "li", function() {
                        if ($("body").hasClass("mobile")) {
                            $("#alfresco-journals #alf-filters").hide();
                            $("#alfresco-journals #alf-content .toolbar .sidebar-toggle").removeClass("yui-button-selected");
                        }
                    });
            } else {
                $("#alfresco-journals #alf-filters")
                    .show()
                    .off("click.sidebar-toggle", "li");
                $("#alfresco-journals #alf-content .toolbar .sidebar-toggle").removeClass("yui-button-selected");
            }
        }
    }

    function transformDashboard(isMobile) {
        if (window.location.pathname.indexOf("dashboard") != -1) {
            var gridContainer = $("#bd .grid");

            if (isMobile) {
                gridContainer.attr("class", "grid");
                if (!gridContainer.attr("data-dashlet-clickable")) {
                    $(".dashlet .title", gridContainer).bind("click.title-clickable", function(event) {
                        $($(event.target).parent()).children().filter(":not(.title)").toggle();
                    });
                    gridContainer.attr("data-dashlet-clickable", "true");
                }
            } else {
                $(".dashlet").children().filter(":not(.title)").show();
                if (gridContainer.attr("data-dashlet-clickable")) {
                    $(".dashlet .title", gridContainer).unbind("click.title-clickable");
                    gridContainer.removeAttr("data-dashlet-clickable");
                }
                gridContainer.attr("class", gridContainer.attr("data-class-backup"));
            }

            viewportToggle(isMobile);
        }
    };

    function transformForm(isMobile) {
        if (/node-(create|edit)-page/.test(window.location.pathname)) {
            viewportToggle(isMobile);
        }
    };

})()
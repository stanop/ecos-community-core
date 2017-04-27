// WORK PROCESS
(function() {

    if (Citeck.mobile.isMobileDevice()) {       
        YAHOO.Bubbling.on("on-mobile-device", function(e, args) { if (args[1].fn) fn(); });

        $(document).ready(function() {
            $("body").addClass("mobile-device")

            // global mobile style
            mobileGlobalClassToggle(true)

            // viewport only for dashboard (while development process)
            // and for all pages in production after all tests
            transformDashboard(true);
            transformJournalsSidebar(true);
            transformForm(true);
            trasformCard(true);
        });
    } else {       
        $(document).ready(function() {
            var mobileWidth = (function() {
                var defaultMobileWidth = { "default": 525, "card": 700 }, 
                    themesMobileWidth = {
                        "yui-skin-citeckTheme": { "default": 585, "card": 670 },
                        "yui-skin-uedmsTheme": { "default": 543 }
                    };

                var bodyThemeClass = document.getElementById("Share").className.match(/yui-skin-\w+Theme/),
                    yuiThemeName = bodyThemeClass ? bodyThemeClass[0] : "",
                    page = "default";

                if (/card-details/.test(window.location.pathname)) { page = "card"; }

                if (themesMobileWidth[yuiThemeName] && themesMobileWidth[yuiThemeName][page]) {
                    return themesMobileWidth[yuiThemeName][page];
                }
                return defaultMobileWidth[page];
            })();

            // for dashboard
            $("#bd .grid").attr("data-class-backup", $("#bd .grid").attr("class"));

            // for card-details
            $.each($("#bd .yui-gc .yui-u"), function(index, group) {
              $.each($(".cardlet", group), function(position, cardlet) {
                $(cardlet).attr("data-location", index + "-" + position)
              });
            });


            $(window).resize(function(event) {
                var functions = [ 
                        transformJournalsSidebar, 
                        transformDashboard,
                        transformForm,
                        trasformCard
                    ],
                    isMobile = window.innerWidth <= mobileWidth;

                if (mobileGlobalClassToggle(isMobile)) {
                    for (var f in functions) { functions[f](isMobile); }
                }
            });

            $(window).resize();
        });
    }


    function mobileGlobalClassToggle(isMobile) {
        var isDifferentState = isMobile != !!$("body").hasClass("mobile");
        if (isDifferentState) YAHOO.Bubbling.fire("change-mobile-mode", { mobileMode: isMobile });
        isMobile ? $("body").addClass("mobile") : $("body").removeClass("mobile");
        return isDifferentState;
    }

    function viewportToggle(isMobile) {
        if (isMobile) {
            $("head").append($("<meta>", { 
                name: "viewport", 
                content: "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" 
            }));
        } else { $("meta[name='viewport']").remove(); }
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

    function trasformCard(isMobile) {
        if (/card-details/.test(window.location.pathname)) {
            var moveCardlets = function(flag) {
                if (flag) {
                    // move all cardlets with negative position index
                    $("#bd .yui-gc .cardlet[data-position-index-in-mobile=-1]").appendTo($("#bd .yui-gc > .yui-u:not(.first)"));

                    // move all cardlets with positive position index
                    $("#bd .yui-gc .cardlet[data-position-index-in-mobile!=-1]").sort(function(a, b) { 
                        var ap = parseInt(a.getAttribute("data-position-index-in-mobile")),
                            bp = parseInt(b.getAttribute("data-position-index-in-mobile"));
                        return ap > bp ? 1 : -1; 
                    }).prependTo($("#bd .yui-gc > .yui-u.first"));
                } else {
                    for (var i = 0; i <= 1; i++) {
                        $("#bd .yui-gc .cardlet[data-location^=" + i + "]").sort(function(a, b) {
                            var al = parseInt(a.getAttribute("data-location").split("-")[1]),
                                bl = parseInt(b.getAttribute("data-location").split("-")[1]);
                            return al > bl ? 1 : -1; 
                        }).appendTo($("#bd .yui-gc > .yui-u:nth-child(" + (i + 1) + ")"));
                    }
                }
            };

            if (isMobile) {
                $("#bd .yui-gc .cardlet[data-available-in-mobile=false]").hide(500);
                moveCardlets(isMobile);
            } else {
                moveCardlets(isMobile);
                $("#bd .yui-gc .cardlet:hidden").show(500);
            }
        }

        viewportToggle(isMobile);
    }



})()
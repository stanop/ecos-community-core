define(['dojo/_base/declare',
        'dijit/_WidgetBase',
        'dijit/_TemplatedMixin',
        'alfresco/core/Core',
        'alfresco/core/CoreWidgetProcessing',
        'jquery',
        'jquerymmenu',
        'jqueryscrollbar'],

    function(declare, _WidgetBase, _TemplatedMixin, AlfCore, CoreWidgetProcessing, $) {
        return declare([_WidgetBase, _TemplatedMixin, AlfCore], {
            templateString: "<div/>",
            postMixInProperties: function  header_citeckMainSlideMenu__postMixInProperties() {
                var menu = '', self = this;
                if (this.isMobile) {
                    $('#HEADER_APP_MENU_BAR').append(
                        '<span class="hamburger-mobile-menu">'+
                            '<a href="#menu" id="hamburger" class="mm-slideout">'+
                                '<span class="hamburger hamburger--collapse">'+
                                    '<span class="hamburger-box">'+
                                         '<span class="hamburger-inner"/>'+
                                    '</span>'+
                                '</span>'+
                            '</a>'+
                        '</span>');
                }

                if (this.widgets && this.widgets.length) {
                    menu = '<ul>' + add_new_ul(this.widgets) + '</ul>';
                }

                function add_new_ul(widgets) {
                    var ul = "";
                    widgets.forEach(function (item) {
                        if (item.sectionTitle) {
                            ul += '<li class="mm-listitem_divider" id="' + item.id + '"><i class="fa fa-menu-section-icon"/><span class="menu-section-title">' + self.message(item.sectionTitle) + '</span></li>';
                        } else {
                            ul += '<li id="' + item.id + '">';
                            var label = '<i class="fa fa-menu-default-icon ' + item.id
                                                + (item.iconImage ? '"style="background-image:url(' + item.iconImage + ')"' : "") + ' "></i>' + self.message(item.label || "header." + item.id + ".label");
                            if (item.url) {
                                ul += '<a href="' + item.url + '" onclick="' + "$('#menu').data('mmenu').close()" + '">' + label + '</a>';
                            } else if (item.clickEvent) {
                                ul += '<a href="#" onclick="' + "$('#menu').data('mmenu').close();\n" + item.clickEvent + '">' + label + '</a>';
                            } else {
                                ul += '<span>' + label + '</span>';
                            }
                        }
                        if (item.widgets && item.widgets.length) {
                            ul += item.sectionTitle ? add_new_ul(item.widgets) : '<ul>' + add_new_ul(item.widgets) + '</ul>';
                        }
                        ul += !item.sectionTitle ? '</li>' : "";
                    });

                    return ul;
                }

                $('body').append('<nav id = "menu">' + menu + '</nav>');

                var menuOnclick = "$('#menu').data('mmenu').close()";
                var header =
                    '<a class="mm-btn_menu" style="display: inline;" href="#" onclick="' + menuOnclick + '" id="hamburger" class="mm-slideout">' +
                    '    <span class="hamburger hamburger--collapse">' +
                    '        <span class="hamburger-box">' +
                    '            <span class="hamburger-inner"></span>' +
                    '        </span>' +
                    '    </span>' +
                    '</a>' +
                    '<span style="display: inline; float: left">';

                if (this.logoSrcMobile) {
                    header += '<img class="menu-mobile-icon" src='+ this.logoSrcMobile +'></img>'
                }

                if (this.logoSrc) {
                    header +=
                        '<a href="/share/page"' + ' onclick="' + "$('#menu').data('mmenu').close()" + '">' +
                        '    <img src='+ this.logoSrc + '></img>' +
                        '</a>';
                }
                header += '</span>';

                $('#menu').mmenu({
                    "slidingSubmenus": false,
                    "extensions": [
                        "pagedim-black"
                    ],
                    "dividers": {
                        "fixed": false
                    },
                    "iconPanels": {
                        "add": true,
                        "size": 60,
                        "hideDivider": true,
                        "hideNavbar": true,
                        "visible": "first"
                    },
                    "offCanvas": {
                        "position": "bottom",
                        "zposition": "front"
                    },
                    "setSelected": {
                        "hover": true,
                        "parent": true
                    },
                    "sidebar": {
                        "collapsed": {
                            "use": 550,
                            "size": 60,
                            "hideNavbar": true,
                            "hideDivider": true
                        }
                    },
                    "searchfield": {
                        "panel": {
                            "add": true
                        }
                    },
                    "navbar": {
                        "add": false
                    },
                    "navbars": [
                        {
                            "position": "header",
                            "content": [
                                header
                            ]
                        }
                    ]
                }, {
                    "searchfield": {
                        "clear": true
                    },
                    "selectedClass": "active"
                });
            },
            postCreate: function header_citeckMainSlideMenu__postCreate() {
                $(document).ready(
                    function() {
                        $(".mm-panel").niceScroll();
                    }
                );
            }
        });
    }
);
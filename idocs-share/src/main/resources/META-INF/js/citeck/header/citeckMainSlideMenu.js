define(['dojo/_base/declare',
        'dijit/_WidgetBase',
        'dijit/_TemplatedMixin',
        'dojo/text!../templates/JQueryMenu.html',
        'alfresco/core/Core',
        'alfresco/core/CoreWidgetProcessing',
        'jquery',
        'jquerymmenu'],

    function(declare, _WidgetBase, _TemplatedMixin, template, AlfCore, CoreWidgetProcessing, $) {
        return declare([_WidgetBase, _TemplatedMixin, AlfCore], {
            templateString:  template,
            postCreate: function header_citeckMainSlideMenu__postCreate() {
                var menu = '', self = this;
                if (this.widgets && this.widgets.length) {
                    menu = '<ul>' + add_new_ul(this.widgets) + '</ul>';
                }

                function add_new_ul(widgets) {
                    var ul = "";
                    widgets.forEach(function (item) {
                        if (item.sectionTitle) {
                            ul += '<li class="mm-listitem_divider" id="' + item.id + '">' + self.message(item.sectionTitle) + '</li>';
                        } else {
                            ul += '<li id="' + item.id + '">';
                            var label = '<i class="fa fa-menu-default-icon ' + item.id + '"></i>' + self.message(item.label);
                            if (item.url) {
                                ul += '<a href="' + item.url + '">' + label + '</a>';
                                return;
                            }
                            if (item.clickEvent) {
                                ul += '<a href="#" onclick="' + "$('#menu').data('mmenu').close();\n" + item.clickEvent + '">' + label + '</a>';
                                return;
                            }
                            ul += '<span>' + label + '</span>';
                        }
                        if (item.widgets) {
                            ul += item.sectionTitle ? add_new_ul(item.widgets) : '<ul>' + add_new_ul(item.widgets) + '</ul>';
                        }
                        ul += !item.sectionTitle ? '</li>' : "";
                    });

                    return ul;
                }

                $('body').append('<nav id = "menu">' + menu + '</nav>');

                $('#menu').mmenu({
                    "slidingSubmenus": false,
                    "extensions": [
                        "pagedim-black"
                    ],
                    "dividers": {
                        "fixed": true
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
                        },
                        "expanded": 1430
                    },
                    "searchfield": {
                        "panel": {
                            "add": true
                        }
                    }
                }, {
                    "searchfield": {
                        "clear": true
                    }
                });

                if (this.logoSrc) {
                    $('.mm-navbar').append('<a href="/share/page"><img src='+ this.logoSrc +'></img></a>')
                }
            }
        });
    }
)
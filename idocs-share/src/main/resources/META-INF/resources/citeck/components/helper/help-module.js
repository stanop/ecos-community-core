/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
(function () {

    if (typeof Citeck == "undefined") Citeck = {};
    Citeck.module = Citeck.module || {};

    Citeck.module.HelpModule = function (id) {
        Citeck.module.HelpModule.superclass.constructor.call(this, "Citeck.module.HelpModule", id, [ "container", "tabview" ]);
        this.configs = [];
        YAHOO.Bubbling.on("showInfo", this._showInfo, this);

        $(window).load(this.bind(function () {
            var pageId = location.href.replace(/^[^?]*[/]([^/?]+)([?].*)?$/, "$1");
            if (pageId) {
                this._loadConfig(pageId);
            }
            this._loadConfig();
        }));

    };

    YAHOO.extend(Citeck.module.HelpModule, Alfresco.component.Base, {

        onReady: function () {

            this.widgets.dialog = new YAHOO.widget.Panel(this.id + '-dialog', {
                width: "720px",
                height: "420px",
                fixedcenter: true
            });
            this.widgets.tabView = new YAHOO.widget.TabView(this.id + '-tabs');

        },


        _loadConfig: function (pageId) {

            var removeTooltip = function (tooltip) {
                if (tooltip.is(':hover')) {
                    return
                }
                tooltip.remove()
            }
            if (!pageId) pageId = "";
            this.configs.data = {}
            var url = '/share/proxy/alfresco/citeck/search/help?type=help:configuration&sitePage=' + pageId;
            $.getJSON(url, this.bind(function (data) {
                for (var i = 0; i < data.nodes.length; i++) {
                    var el = $(data.nodes[i].selector);

                     var info = data.nodes[i]
                    this.configs.data[info.selector] = info;
                    el.live("mouseover",{info: info},  function () {
                        var line = $(this);
                        var pos = line.offset()
                        var info = arguments[0].data.info
                        var infoText = info.infoText;
                        var tooltip = jQuery('#help-tooltip');
                        tooltip.remove();
                        var links = ''
                        if (info.text != null || info.video != null) {
                            links = "<a class=\"video-link link\" onclick=\"YAHOO.Bubbling.fire('showInfo', {selector :'"+encodeURIComponent(info.selector)+"'})\">Подробнее</a>"
                        }
                        var tooltipContent = "<div id='help-tooltip' class='tooltip' >" +
                            "<div id='content'>" + infoText + "</div>" +
                            "<div id='help-tooltip-footer'>" + links + "</div>" +
                            "</div>";

                        jQuery('body').append(tooltipContent)
                        tooltip = jQuery('#help-tooltip');
                        var body = jQuery('body');
                        var left = body.scrollLeft() + body.width() >  pos.left + line.width() + 10 + tooltip.width() ?  pos.left + line.width() + 5 : pos.left - 5 - tooltip.width()
                        var top = body.scrollTop() - 5 <   pos.top - 30 ?  pos.top - 30 : body.scrollTop() +3
                        tooltip.css({left: left, top: top})
                        tooltip.show()
                        tooltip.live("mouseleave", function () {
                            setTimeout(function () {
                                removeTooltip(tooltip)
                            }, 1000)
                        });
                    });
                    el.live("mouseleave", function () {
                        var tooltip = jQuery('#help-tooltip');
                        setTimeout(function () {
                            removeTooltip(tooltip)
                        }, 1000)

                    });

                }
            }));
        },
        _showInfo: function (event, data, ev) {
            var videoTab = Dom.get(this.id + "-tab-video");
            var textTab = Dom.get(this.id + "-tab-text");
             var info = this.configs.data[decodeURIComponent(data[1].selector)]
            var textNodeRef = info.text;
            var videoNodeRef =info.video;
            if (textNodeRef) {
                var textURL = Alfresco.constants.PROXY_URI + "api/node/" + textNodeRef.replace(":/", "") + "/content/thumbnails/webpreview?c=force&lastModified=webpreview";
                textTab.innerHTML = '<embed type="application/x-shockwave-flash" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/preview/WebPreviewer.swf" width="100%" height="400px" style="" id="WebPreviewer_text_info" name="WebPreviewer_text_info" quality="high" allowscriptaccess="sameDomain" allowfullscreen="true" wmode="transparent" flashvars="fileName=&amp;paging=true&amp;url=' + textURL + '&amp;i18n_actualSize=Реальный размер&amp;i18n_fitPage=Подогнать страницу&amp;i18n_fitWidth=Подогнать Ширину&amp;i18n_fitHeight=Подогнать Высоту&amp;i18n_fullscreen=На весь экран&amp;i18n_fullwindow=Увеличить&amp;i18n_fullwindow_escape=Для выхода из полноэкранного режима нажмите Esc&amp;i18n_page=Страница&amp;i18n_pageOf=из&amp;show_fullscreen_button=true&amp;show_fullwindow_button=false&amp;disable_i18n_input_fix=false">';
                this.widgets.tabView.selectTab(0);
                this.widgets.tabView.getTab(0).setStyle("display", "inline-block");
            } else {
                this.widgets.tabView.getTab(0).setStyle("display", "none");
            }

            if (videoNodeRef) {
                var videoURL = Alfresco.constants.PROXY_URI + "api/node/" + videoNodeRef.replace(":/", "") + "/content/" + info.type.replace("/",".");
                var so = new SWFObject(Alfresco.constants.URL_RESCONTEXT + 'citeck/components/helper/player.swf', 'mpl', '700', '400', '8');
                so.addParam('allowfullscreen', 'true');
                so.addParam('flashvars', 'file=' + encodeURIComponent(videoURL) + '&autostart=true');
                so.write(this.id + '-player');

                this.widgets.tabView.selectTab(1);
                this.widgets.tabView.getTab(1).setStyle("display", "inline-block");
            } else {
                this.widgets.tabView.getTab(1).setStyle("display", "none")
            }
            this.widgets.dialog.render();
            jQuery('.container-close').bind('click',{id:'global_x002e_header_x0023_Help_Information-player'},function(){
                jQuery('#'+arguments[0].data.id).html('')
            })
             this.widgets.dialog.show();

        }

    });


})();
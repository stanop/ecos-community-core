/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @module alfresco/logo/Logo
 * @extends dijit/_WidgetBase
 * @mixes dijit/_TemplatedMixin
 * @mixes module:alfresco/core/Core
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "alfresco/core/Core",
        "dojo/dom-construct",
        "dojo/dom-style",
        "js/citeck/_citeck.lib",
        "dojo/dom-class"],
    function (declare, _Widget, _Templated, Core, domConstruct, domStyle, _citecklib, domClass) {

        return declare([_Widget, _Templated, Core], {

            /**
             * An array of the CSS files to use with this widget.
             *
             * @instance
             * @type {{cssFile: string, media: string}[]}
             * @default [{cssFile:"./css/Logo.css"}]
             */
            cssRequirements: [{cssFile: "./css/Logo.css"}],

            /**
             * The CSS class or classes to use to generate the logo
             * @instance
             * @type {string}
             * @default "alfresco-logo-large"
             */
            logoClasses: "alfresco-logo-large",

            /**
             * @instance
             * @type {string}
             */
            logoSrc: null,

            /**
             *
             * @instance
             * @type {string}
             * @default "display: none;"
             */
            cssNodeStyle: "display: none;",

            /**
             *
             * @instance
             * @type {string}
             * @default "display: none;"
             */
            imgNodeStyle: "display: none;",

            /**
             * The HTML template to use for the widget.
             * @instance
             * @type {string}
             */
            templateString: "",

            /**
             * This controls whether or not the image is rendered with the img element or the div in the template.
             * The default it to use the div because it is controlled via CSS which allows for finer control over the
             * dimensions of the displayed logo. When using the img element the dimensions will be those of the supplied
             * image.
             *
             * @instance
             */

            targetUrl: null,

            buildRendering: function alfresco_logo_Logo__buildRendering() {
                this.templateString = '<div class="logo alfresco-logo-Logo"><a href="/share/page/' + this.targetUrl + '"><img src= "' + this.logoSrc + '" style="display: block;"></a></div>';
                if (this.logoSrc) {
                    this.imgNodeStyle = "display: block;";
                }
                else {
                    this.cssNodeStyle = "display: block";
                }
                this.inherited(arguments);
            },

            postCreate: function alfresco_logo_Logo__postCreate() {
                domClass.add(this.domNode, "alfresco-logo-Logo");
                var self = this;
                this.domNode.addEventListener("click", function(event) {
                    event.stopPropagation();
                    if (self.targetUrl) {
                        self.alfPublish("ALF_NAVIGATE_TO_PAGE", { url: self.targetUrl, type: self.targetUrlType, target: self.targetUrlLocation});
                    } else {
                        self.alfLog("error", "An AlfMenuItem was clicked but did not define a 'targetUrl' or 'publishTopic' or 'clickEvent' attribute", event);
                    }
                }, false);
                this.inherited(arguments);
            }
        });
    }
);
(function() {
    Citeck = typeof Citeck != "undefined" ? Citeck : {};
    Citeck.widget = Citeck.widget || {};

    Citeck.widget.CaseStatus = function(htmlid) {
        Citeck.widget.CaseStatus.superclass.constructor.call(this, "Citeck.widget.CaseStatus", htmlid, null);

        YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
    };

    YAHOO.extend(Citeck.widget.CaseStatus, Alfresco.component.Base);

    YAHOO.lang.augmentObject(Citeck.widget.CaseStatus.prototype, {
        // default values for options
        options: {
            nodeRef: null,
            isPendingUpdate: false,
            loaded: false,
            htmlid: null,
            emptyMsg: "-"
        },
        onReady: function() {
            if (this.options.isPendingUpdate) {
                this.checkPendingUpdate();
            } else {
                this.loadStatus();
            }
        },
        loadStatus: function () {

            var options = this.options;

            var statusNameEl = $("#" + options.htmlid + "-statusName");
            var statusHeaderEl = $("#" + options.htmlid + "-header");

            statusNameEl.html("");
            statusNameEl.addClass("loading");

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + 'citeck/case/status?nodeRef=' + this.options.nodeRef,
                successCallback: {
                    scope: this,
                    fn: function (response) {
                        statusNameEl.html(response.json.statusName || this.msg("status.empty"));
                        statusHeaderEl.html(this.msg("header." + response.json.statusType));
                        statusNameEl.removeClass("loading");
                    }
                },
                failureCallback: {
                    fn: function (response) {
                        console.error(response);
                    }
                }
            });
        },
        checkPendingUpdate: function() {

            var url = 'citeck/node/check-pending-update?nodeRef=' + this.options.nodeRef;

            var onSuccess = function(response) {
                if (response.json == false) {
                    YAHOO.Bubbling.fire("metadataRefresh");
                    this.loadStatus();
                } else {
                    this.checkPendingUpdate();
                }
            };

            var onFailure = function (response) {
                console.error(response);
            };

            var check = function() {
                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + url,
                    successCallback: {
                        scope: this,
                        fn: onSuccess
                    },
                    failureCallback: {
                        fn: onFailure
                    }
                });
            };

            var scope = this;
            setTimeout(function() {check.call(scope);}, 2000);
        },
        doRefresh: function CaseStatus_doRefresh() {
            this.loadStatus();
        }

    }, true);

})();

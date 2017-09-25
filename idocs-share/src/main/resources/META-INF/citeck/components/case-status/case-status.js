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
			// parent node reference
			nodeRef: null,
			isPendingUpdate: false
		},
		onReady: function() {
			if (this.options.isPendingUpdate) {
				this.checkPendingUpdate();
			}
		},
		checkPendingUpdate: function() {

			var url = '/citeck/node/check-pending-update?nodeRef=' + this.options.nodeRef;

			var onSuccess = function(response) {
				if (response.json == false) {
					YAHOO.Bubbling.fire("metadataRefresh");
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
		doRefresh: function CaseStatus_doRefresh()
		{
			YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
			this.refresh('/citeck/components/case-status?nodeRef={nodeRef}' + (this.options.siteId ? '&site={siteId}' : ''));
		}

	}, true);

})();

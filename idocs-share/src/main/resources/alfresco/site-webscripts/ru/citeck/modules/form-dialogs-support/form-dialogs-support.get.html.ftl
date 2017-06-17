<script type="text/javascript">//<![CDATA[
(function() {

	// unsubscribe manager from formContentReady
	var unsubscribeUnsafeMgr = function(eventName, handlerName, mgrName) {
		var unsubscribe = function() {
			var mgr = Alfresco.util.ComponentManager.findFirst(mgrName);
			YAHOO.lang.later(0, null, function() {
				YAHOO.Bubbling.unsubscribe(eventName, mgr[handlerName], mgr);
				YAHOO.Bubbling.unsubscribe(eventName, unsubscribe);
			});
		}
		YAHOO.Bubbling.on(eventName, unsubscribe);
	};

	unsubscribeUnsafeMgr("beforeFormRuntimeInit", "onBeforeFormRuntimeInit", "${args.formManagerName}");
	unsubscribeUnsafeMgr("formContentReady", "onFormContentReady", "${args.formManagerName}");
})();
//]]></script>

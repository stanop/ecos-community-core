(function() {

    if (Citeck.mobile.isMobileDevice() && Citeck.mobile.hasTouchEvent()) {
        
        // share global handler
        YAHOO.Bubbling.on("on-mobile-device", function(e, args) { 
            if (args.fn) fn();
        });       

        // DASHBOARD PAGE (for development only)
        if (window.location.pathname.indexOf("dashboard") != -1) {
	        $("head").append(
	        	$("<meta>", { name: "viewport", content: "width=device-width, initial-scale=1.0" })
	        );

	        $(document).ready(function() {
		        $("#bd .grid").attr("class", "grid")
	        });        
        }
    }

})()
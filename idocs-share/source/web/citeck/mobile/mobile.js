
(function() {

    if (Citeck.mobile.isMobileDevice() && Citeck.mobile.hasTouchEvent()) {       
        // share global handler
        YAHOO.Bubbling.on("on-mobile-device", function(e, args) { 
            if (args.fn) fn();
        });

        $(document).ready(function() {
	        // global mobile style
	        $("body").addClass("mobile");

	        // viewport only for dashboard and forms (while development process)
	        var formPages = ["node-create-page", "node-edit-page", "dashboard"];
	        for (var fp in formPages) {
	        	if (window.location.pathname.indexOf(formPages[fp]) != -1) {
			        $("head").append(
			        	$("<meta>", { name: "viewport", content: "width=device-width, initial-scale=1.0" })
			        );
			        
			        switch (formPages[fp]) {
			        	case "dashboard":
			        		$("#bd .grid").attr("class", "grid");
			        		break;
			        }
			         
			        break;
	        	}
	        }
    	});
    }

})()
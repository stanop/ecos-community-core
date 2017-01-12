(function() {

    if (!args.activityRef) {
        status.code = 400;
        status.message = "activityRef is a mandatory parameter";
        status.redirect = true;
        return;
    }

    if (!args.parentRef && !args.index) {
        status.code = 400;
        status.message = "Either parentRef, or index parameters should be set";
        status.redirect = true;
        return;
    }

    if (args.parentRef) {
        caseActivityService.setParent(args.activityRef, args.parentRef);
        if (!args.index) {
            caseActivityService.setIndex(args.activityRef, Packages.java.lang.Integer.MAX_VALUE);
        }
    }

    if (args.index) {
        caseActivityService.setIndex(args.activityRef, args.index);
    }

    status.code = 200;
    status.redirect = true;
})()
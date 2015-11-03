(function(){
    var available = args.available;
    if(available && available != "") {
        available = (available == "true");
    } else {
        available = !availability.getCurrentUserAvailability();
    }
    availability.setCurrentUserAvailability(available);
    model.available = availability.getCurrentUserAvailability();
})();
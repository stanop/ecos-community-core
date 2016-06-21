function process(){
    var luc = "TYPE:\"bcal:calendar\" AND @bcal\\:remark:\"day-off\"";
    var unworkingDays = search.luceneSearch(luc);
    var arrUnworkDays = [];
    var tempDate;
    for each(var unWork in unworkingDays) {
        tempDate = unWork.properties["bcal:dateFrom"];
        while (tempDate < unWork.properties["bcal:dateTo"]) {
            arrUnworkDays.push(tempDate);
            tempDate = new Date(tempDate.getTime() + 24 * 60 * 60 * 1000);
        }
        arrUnworkDays.push(unWork.properties["bcal:dateTo"]);
    }
    var startDate = Packages.org.joda.time.DateTime.now();
    var time = startDate.getMillis() + precedence.stages.get(stageIndex).confirmers.get(0).amountHours * 60 * 60 * 1000;
    var endDate = new Packages.org.joda.time.DateTime(time);
    var someInterval = new Packages.org.joda.time.Interval(startDate, endDate);
    var i = 0;
    for each (var day in arrUnworkDays) {
        if (someInterval.contains(day)) {
            i++;
            someInterval = someInterval.withEnd(someInterval.getEnd().plusDays(1));
        }
    }
    var newEndDate = endDate.plusDays(i);
    execution.setVariable("bpm_workflowDueDate", newEndDate.toDate());
}
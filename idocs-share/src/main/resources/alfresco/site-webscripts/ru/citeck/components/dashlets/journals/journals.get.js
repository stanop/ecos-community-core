function main() {
    var dashletResizer = {
        id : "DashletResizer",
        name : "Alfresco.widget.DashletResizer",
        initArgs : ["\"" + args.htmlid + "\"", "\"" + instance.object.id + "\""],
        useMessages: false
    };

    model.widgets = [dashletResizer];
}

main();
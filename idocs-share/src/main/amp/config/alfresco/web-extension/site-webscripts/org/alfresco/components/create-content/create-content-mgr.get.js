function main() {
   // Widget instantiation metadata...
   var widget = {
      id : "CreateContentMgr", 
      name : "Alfresco.CreateContentMgr",
      options : {
         siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
         isContainer: "true" == ((page.url.args.isContainer != null) ? page.url.args.isContainer : "false")
      }
   };

   var onsubmit = page.url.args.onsubmit || args.onsubmit,
       redirect = page.url.args.redirect || args.redirect;
   if (onsubmit) widget.options.onsubmit = onsubmit;
   if (redirect) widget.options.redirect = "true" == ((page.url.args.redirect != null) ? page.url.args.redirect : "false");

   model.widgets = [widget];
}
main();
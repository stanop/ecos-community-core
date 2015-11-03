function main() {
   // Widget instantiation metadata...
   var widget = {
      id : "TypeManager", 
      name : "Alfresco.TypeManager",
      options : {
         nodeRef: "alfresco://type/root"
      }
   };
   model.widgets = [widget];
}
main();
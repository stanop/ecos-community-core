var nodeRef = args.nodeRef;
var type= url.extension;

function getNodeOwner(nodeRef){
  var userName =  ownerService.getOwner(nodeRef);
   var personService = services.get('PersonService');

  var personNodeRef =  personService.getPerson(userName);
    var userNode = search.findNode(personNodeRef);
    var name = userNode.properties['firstName']
    var lastName = userNode.properties['lastName']
    var login = userNode.properties['userName']
    if((name != null  && name.length > 0)  || (lastName != null && lastName.length > 0)){
        model.owner = name + ' '+ lastName + ' ('+login+')';
    }else{
        model.owner = login;
    }

}
function setNodeOwner(nodeRef){
    var userName = args.owner;

    ownerService.setOwner(nodeRef, userName)

    status.message = true;
}

if(type == 'get'){
    getNodeOwner(nodeRef)
}else if(type == 'set'){
    setNodeOwner(nodeRef)
}


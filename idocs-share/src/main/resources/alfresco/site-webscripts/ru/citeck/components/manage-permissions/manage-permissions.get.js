<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
var nodeRef = page.url.args['nodeRef']
var documentDetails = AlfrescoUtil.getNodeDetails(nodeRef);
model.nodeBrief = documentDetails['item']['displayName'];

var rootNode = "alfresco://company/home", repoConfig = config.scoped["RepositoryLibrary"]["root-node"];
if (repoConfig !== null) {
    rootNode = repoConfig.value
}

var field = {"control": {"params": {"compactMode": true}}};

model.rootNode = rootNode;
model.field = field;
var ownerName = '';
try {
    var connector = remote.connect('alfresco');
    var res = connector.get('/node/owner/get?nodeRef='+nodeRef)
      var responseObj = eval('('+res+')')
    ownerName = responseObj.ownerName;
} catch (ex) {

}
model.owner=ownerName

model.supportedPermissions = [
      {
          group: 'elementary',
          permissions: [
              'FullControl',
              'ReadProperties',
              'ReadChildren',
              'WriteProperties',
              'ReadContent',
              'WriteContent',
              'ExecuteContent',
              'DeleteNode',
              'DeleteChildren',
              'CreateChildren',
              'LinkChildren',
              'DeleteAssociations',
              'ReadAssociations',
              'CreateAssociations',
              'ReadPermissions',
              'ChangePermissions'
          ]
      },
      {
          group: 'basic',
          permissions: [
              'Read',
              'Write',
              'Delete',
              'AddChildren',
              'Execute'
          ]
      },
      {
          group: 'roles',
          permissions: [
              'Administrator',
              'Coordinator',
              'Collaborator',
              'Contributor',
              'Editor',
              'Consumer',
              'RecordAdministrator',
              'All'
          ]
      },
      {
          group: 'sites',
          permissions: [
              'SiteManager',
              'SiteCollaborator',
              'SiteContributor',
              'SiteConsumer'
          ]
      }
];



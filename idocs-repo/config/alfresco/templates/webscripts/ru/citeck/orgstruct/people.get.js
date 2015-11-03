<import resource="classpath:alfresco/templates/webscripts/ru/citeck/orgstruct/orgstruct.lib.js">
model.authorities = groups.searchUsers("*", utils.createPaging(-1,0), "userName");
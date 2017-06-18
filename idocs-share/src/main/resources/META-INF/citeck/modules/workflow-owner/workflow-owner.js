/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
if (Alfresco.component.WorkflowForm) {

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $userProfileLink = Alfresco.util.userProfileLink;

        
    Alfresco.component.WorkflowForm.prototype.renderCellOwner= function WorkflowForm_customRenderCellOwner(elCell, oRecord, oColumn, oData)
    {
        var control = this;
        var owner = oRecord.getData("owner");
        var properties = oRecord.getData("properties");
        if (owner != null && owner.userName)
        {
            var displayName = $html(this.msg("field.owner", owner.firstName, owner.lastName));
            if(properties.bpm_pooledActors && properties.bpm_pooledActors[0])
            {
                var nodeRef = properties.bpm_pooledActors[0];
                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "api/orgstruct/authority?nodeRef=" + nodeRef,
                    successCallback: {
                        fn: function(response) {
                            var division = response.json.displayName;
                            if($html(control.msg("field.ownerWithDivision", owner.firstName, owner.lastName, division))!="field.ownerWithDivision")
                                displayName = $html(control.msg("field.ownerWithDivision", owner.firstName, owner.lastName, division));
                            elCell.innerHTML = $userProfileLink(owner.userName, owner.firstName && owner.lastName ? displayName : null, null, !owner.firstName);
                        }
                    },
                    failureCallback: {
                        fn: function(response) {
                            elCell.innerHTML = $userProfileLink(owner.userName, owner.firstName && owner.lastName ? displayName : null, null, !owner.firstName);
                        }
                    }
                });
            }
            elCell.innerHTML = $userProfileLink(owner.userName, owner.firstName && owner.lastName ? displayName : null, null, !owner.firstName);
        }
        else if(properties.bpm_pooledActors[0]){
            var nodeRef = properties.bpm_pooledActors[0];
            //var url = '/slingshot/doclib2/node/' + nodeRef.replace('://', '/');
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/orgstruct/authority?nodeRef=" + nodeRef,
                successCallback: {
                    fn: function(response) {
                        elCell.innerHTML = response.json.displayName;
                    }
                },
                failureCallback: {
                    fn: function(response) {
                        elCell.innerHTML = this.msg("label.none");
                    }
                }
            });

            elCell.innerHTML = properties.bpm_pooledActors[0];
        }
        else {
            elCell.innerHTML = this.msg("label.none");
        }
    };
}
})();
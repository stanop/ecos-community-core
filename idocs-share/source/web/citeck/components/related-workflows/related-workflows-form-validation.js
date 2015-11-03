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


Alfresco.forms.validation.isRelatedWorkflowsComplete = function isRelatedWorkflowsComplete(field, args, event, form, silent, message) {
    var url = document.location.href.replace(/%24/g,'$');
    var taskId = url.match(/taskId=([^\&]+)/)[1];
    var result = true;
    $.ajax({
        url: Alfresco.constants.PROXY_URI+'api/related-workflows/is-related-workflows-complete',
        type: 'GET',
        data: { taskId : taskId },
        async: false, //query is executed only when events "related_workflows_state_change" so it's ok! ;)
        success: function(json) {
            result = ('true' == json['result']);
        }
    });
    return result;
};
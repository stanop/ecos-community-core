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

if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}
(function() {
	/*
     * YUI Library aliases
     */
    console.log("function");
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;
   // var x = Alfresco.util.ComponentManager.findFirst('Alfresco.component.TaskEditHeader');
    //YAHOO.Bubbling.unsubscribe("taskDetailedData", x.onTaskDetailedData, x);

    /*
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL,
        $userProfileLink = Alfresco.util.userProfileLink;

		
		Citeck.viewWorkflowDiagram = function WD_constructor(htmlId) {
            console.log("WD_constructor");
        Citeck.viewWorkflowDiagram.superclass.constructor.call(this, "Citeck.viewWorkflowDiagram", htmlId);
            console.log("YAHOO.Bubbling.on");
        YAHOO.Bubbling.on("taskDetailedData", this.onTaskDetailsData, this);
        return this;
		};
		
		YAHOO.extend(Citeck.viewWorkflowDiagram, Alfresco.component.Base, {

        options: {
            enable: null
        },
		task: null,
        workflow: null,
       /*	   
	   * @method _displayWorkflowForm
       * @private
       */
	   	   
	   onTaskDetailsData: function TDH_onTaskDetailsData(layer, args) {
           console.log("onTaskDetailsData");
            this.task = args[1];
            var workflowId = this.task.workflowInstance.id;
            if (this.options.enable) {
                var workflowInstancesUrl = YAHOO.lang.substitute(
                    Alfresco.constants.PROXY_URI + "api/workflow-instances/{workflowId}?includeTasks=true", {
                        workflowId: encodeURIComponent(workflowId)
                    }
                );
                YAHOO.util.Connect.asyncRequest(
                    'GET',
                    workflowInstancesUrl, {
                        success: function (o) {
                            if (o.responseText) {
                                console.log("onTaskDetailsData: success");
                                this.workflow = YAHOO.lang.JSON.parse(o.responseText)['data'];
                                //this.loadTasks(this.workflow);
                                //this.printTasks();
								this._loadButtonViewWorkflowDiagram(this.workflow);
                            }
                        },
						failure: this.handleFailure,
                        scope: this
                    }
                );
            }
        },
		
      _loadButtonViewWorkflowDiagram: function WF__loadViewWorkflowDiagram(workflow)
      {
		/*
	   * Called when view workflow diagram button is clicked.
       * WIll display the workflow's diagram.
       */	   
	   //if (this.isReady && this.workflow)
          console.log("_loadButtonViewWorkflowDiagram");
		   if (this.workflow)
         {
            // Display the view diagrambutton if diagram is available
            if (this.workflow.diagramUrl)
            {
                console.log("this.workflow.diagramUrl == true");
               Dom.removeClass(this.id + "-viewWorkflowDiagram");
                //x.widgets.viewWorkflowDiagram = Alfresco.util.createYUIButton(this, "viewWorkflowDiagram", this.onViewWorkflowDiagramClick);
                Alfresco.util.createYUIButton(this, "viewWorkflowDiagram", this.onViewWorkflowDiagramClick);
            }
		 }
	  },
		 
      onViewWorkflowDiagramClick: function(layer, args)
      {
          console.log("onViewWorkflowDiagramClick");
         if (this.workflow.diagramUrl)
         {
            showLightbox({ src: Alfresco.constants.PROXY_URI + this.workflow.diagramUrl });
         }
      },
	  
	   handleFailure: function Grid_handleFailure(o){
           console.log("handleFailure");
            if (o.responseText) {
                alert(o.responseText);
            }
        }
	 
		});	  
})();
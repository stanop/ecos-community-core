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
	var Dom = YAHOO.util.Dom,
		Event = YAHOO.util.Event,
		Selector = YAHOO.util.Selector;
	var $html = Alfresco.util.encodeHTML,
		$hasEventInterest = Alfresco.util.hasEventInterest,
		$siteURL = Alfresco.util.siteURL;

	var x = Alfresco.util.ComponentManager.findFirst('Alfresco.component.TaskEditHeader');
	YAHOO.Bubbling.unsubscribe("taskDetailedData", x.onTaskDetailedData, x);
	YAHOO.Bubbling.on("taskDetailedData", function (layer, args) {
		var task = args[1];
		// Save task id so we can use it when invoking actions later
		x.taskId = task.id;
		// Display actions and create yui buttons
		Selector.query("h1 span", x.id, true).innerHTML = $html(task.title);
		// Inform user that this task has been completed
		if (!task.isEditable)
		{
			Alfresco.util.PopupManager.displayMessage(
			{
				text: Alfresco.util.message('message.task.is.not.editable'),
				displayTime: 3
			});
			YAHOO.lang.later(3000, x, function()
			{
				document.location.href = $siteURL("task-details?taskId=" + x.taskId + "&referrer=tasks&myTasksLinkBack=true");
			}, []);
		}
		if (task.isReassignable)
		{
			// Task is reassignable
			x.widgets.reassignButton = Alfresco.util.createYUIButton(x, "reassign", x.onReassignButtonClick);
			Dom.removeClass(Selector.query(".actions .reassign", x.id), "hidden");
		}
		if (task.isClaimable)
		{
			// Task is claimable
			x.widgets.claimButton = Alfresco.util.createYUIButton(x, "claim", x.onClaimButtonClick);
			Dom.removeClass(Selector.query(".actions .claim", x.id), "hidden");
			Dom.removeClass(Selector.query(".unassigned-message", x.id), "hidden");
		}
		if (task.isReleasable)
		{
			// Task is releasable
			x.widgets.releaseButton = Alfresco.util.createYUIButton(x, "release", x.onReleaseButtonClick);
			Dom.removeClass(Selector.query(".actions .release", x.id), "hidden");
		}
	});
		
})();

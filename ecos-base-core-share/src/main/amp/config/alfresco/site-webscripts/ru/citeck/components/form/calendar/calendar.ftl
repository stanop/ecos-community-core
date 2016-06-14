<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<#macro makeHiddenField name value>
<@forms.renderField field=name extension = {
	"control" : {
		"template" : "/ru/citeck/components/form/controls/always-hidden-textfield.ftl",
		"params": {
			"value": value
		}
	}
} />
</#macro>

<@forms.setMandatoryFields
fieldNames = [
	"prop_ia_whatEvent",
	"prop_ia_fromDate",
	"prop_ia_toDate"
]/>

<#if formUI == "true">
	<@formLib.renderFormsRuntime formId=formId />
</#if>

<#if form.mode == "view">
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
<#else>
	<#assign twoColumnClass = "yui-g" />
	<#assign threeColumnClass = "yui-gb" />
</#if>

<@formLib.renderFormContainer formId=formId>

<#if form.mode == "create">
	<@makeHiddenField "prop_ia_isOutlook" "false" />
</#if>

<@forms.renderField field="prop_ia_whatEvent" />

<@forms.renderField field="prop_ia_whereEvent" />

<@forms.renderField field="prop_ia_descriptionEvent" extension = extensions.controls.textarea />

<script type="text/javascript">//<![CDATA[
	(function() {
		var ZERO_REGEXP = /^(\s*0+):(0+\s*)$/;

		function getHour(time) {
			var result = null,
				timePartsRegexp = /^\s*(\d+):(\d+)\s*$/,
				timeParts = time.match(timePartsRegexp);
			if (timeParts && timeParts[1] && timeParts[2]) {
				var hour = parseInt(timeParts[1]);
				if (!isNaN(hour))
					result = hour;
			}
			return result;
		}

		YAHOO.util.Event.addListener("${args.htmlid}-form", "submit", function(e) {
			var result = true,
				allDayCheckbox = YAHOO.util.Dom.get("${args.htmlid}-allDay"),
				fromTime = YAHOO.util.Dom.get("${args.htmlid}_prop_ia_fromDate-cntrl-time"),
				toTime = YAHOO.util.Dom.get("${args.htmlid}_prop_ia_toDate-cntrl-time");
			if (fromTime && toTime && allDayCheckbox && !allDayCheckbox.checked &&
					(!fromTime.value || !toTime.value)) {
				if (!fromTime.value)
					fromTime.focus();
				else if (!toTime.value)
					toTime.focus();
				result = false;
			}
			return result;
		});

		YAHOO.util.Event.addListener("${args.htmlid}-allDay", "click", function(e) {
			var fromDateCtrl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_ia_fromDate-cntrl"),
				toDateCtrl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_ia_toDate-cntrl"),
				allDayCheckbox = YAHOO.util.Dom.get("${args.htmlid}-allDay");
			if (allDayCheckbox && allDayCheckbox.checked && fromDateCtrl && toDateCtrl) {
				var fromDate = YAHOO.util.Dom.get(fromDateCtrl.currentValueHtmlId),
					toDate = YAHOO.util.Dom.get(toDateCtrl.currentValueHtmlId);
				if (fromDate && toDate && fromDate.value && toDate.value) {
					var fromDateTime = Alfresco.util.fromISO8601(fromDate.value),
						toDateTime = Alfresco.util.fromISO8601(toDate.value);
					if (fromDateTime && toDateTime) {
						var newFromDate = new Date(Date.UTC(
								fromDateTime.getFullYear(),
								fromDateTime.getMonth(),
								fromDateTime.getDate(),
								0, 0, 0
							)),
							newToDate = new Date(Date.UTC(
								toDateTime.getFullYear(),
								toDateTime.getMonth(),
								toDateTime.getDate(),
								0, 0, 0
							));
						fromDateCtrl.setOptions({ currentValue: Alfresco.util.toISO8601(newFromDate) });
						fromDateCtrl.onReady();
						toDateCtrl.setOptions({ currentValue: Alfresco.util.toISO8601(newToDate) });
						toDateCtrl.onReady();
						YAHOO.util.Dom.addClass("${args.htmlid}_prop_ia_fromDate-cntrl-time", "hidden");
						YAHOO.util.Dom.addClass("${args.htmlid}_prop_ia_toDate-cntrl-time", "hidden");
					}
				}
			}
			else if (allDayCheckbox && fromDateCtrl && toDateCtrl) {
				YAHOO.util.Dom.removeClass("${args.htmlid}_prop_ia_fromDate-cntrl-time", "hidden");
				YAHOO.util.Dom.removeClass("${args.htmlid}_prop_ia_toDate-cntrl-time", "hidden");
				var hour = getHour(fromTime.value);
				if (hour !== null) {
					hour++;
					if (hour < 10)
						hour = '0' + hour;
					toTime.value = hour + ':' + timeParts[2];
				}
			}
		});
<#if form.mode == "edit">
		YAHOO.util.Event.onContentReady("${args.htmlid}_prop_ia_toDate", function() {
			var timer = null;
			timer = YAHOO.lang.later(200, this, function() {
				var fromDateCtrl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_ia_fromDate-cntrl"),
					toDateCtrl = Alfresco.util.ComponentManager.get("${args.htmlid}_prop_ia_toDate-cntrl"),
					allDayCheckbox = YAHOO.util.Dom.get("${args.htmlid}-allDay");
				if (fromDateCtrl && toDateCtrl && allDayCheckbox) {
					var fromDate = YAHOO.util.Dom.get(fromDateCtrl.currentValueHtmlId),
						toDate = YAHOO.util.Dom.get(toDateCtrl.currentValueHtmlId);
					if (fromDate && toDate) {
						if (fromDate.value && toDate.value) {
							var fromDateTime = Alfresco.util.fromISO8601(fromDate.value),
								toDateTime = Alfresco.util.fromISO8601(toDate.value);
							if (fromDateTime && toDateTime) {
								var utcFromDate = new Date(Date.UTC(
										fromDateTime.getFullYear(),
										fromDateTime.getMonth(),
										fromDateTime.getDate(),
										0, 0, 0
									));
								if (utcFromDate.getTime() == fromDateTime.getTime() &&
										fromDateTime.getTime() == toDateTime.getTime()) {
									allDayCheckbox.click();
								}
							}
						}
						else {
							// wait until mandatory values is occurred there
							return;
						}
					}
				}
				if (timer)
					timer.cancel();
			}, null, true);
		});
</#if>
	})();
//]]></script>
<#assign isAllDay=false />
<label><input id="${args.htmlid}-allDay" type="checkbox" style="margin: 3px 7px 11px 0" <#if isAllDay == true> checked="checked" </#if> />${msg("calendar.event.allDay")}</label>

<@forms.renderField field="prop_ia_fromDate" extension = {
	"control" : {
		"template" : "/ru/citeck/components/form/controls/date.ftl",
		"params": {
			"showTime": "true",
			"appendDaysToCurrentValue" : 0
		}
	}
} />

<@forms.renderField field="prop_ia_toDate" extension = {
	"control" : {
		"template" : "/ru/citeck/components/form/controls/date.ftl",
		"params": {
			"showTime": "true",
			"appendHoursToCurrentValue" : 1
		}
	}
} />

<@forms.renderField field="prop_cm_taggable" extension = {
	"control" : {
		"template" : "/org/alfresco/components/form/controls/category.ftl",
		"params": {
			"compactMode": true,
			"params": "aspect=cm:taggable",
			"createNewItemUri": "/api/tag/workspace/SpacesStore",
			"createNewItemIcon": "tag"
		}
	}
} />

</@>

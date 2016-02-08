<#include "common/dynamic-tree-picker.inc.ftl" />
<#assign controlId = fieldHtmlId + "-cntrl">

<#assign 
	params = field.control.params!{}
	peopleFieldName = params.peopleField!""
/>

<#assign peopleField = "" />
<#list form.fields?keys as fieldName>
	<#if fieldName == peopleFieldName>
		<#assign peopleField = form.fields[fieldName] />
		<#break />
	</#if>
</#list>

<#if peopleField?is_string>
	<#stop "peopleField must be specified for passports.ftl" />
</#if>

<#assign passportsField = field />

<#macro renderHiddenInputs field databind>
	<#assign is_property = field.type == "property" />
	<input id="${args.htmlid}_${field.name}" type="hidden" name="<#if is_property>${field.name}<#else>-</#if>" data-bind="{ value: ${databind}().join(',') }" />
	<input type="hidden" name="<#if !is_property>${field.name}_added<#else>-</#if>" data-bind="{ value: ${databind}Added().join(',') }" />
	<input type="hidden" name="<#if !is_property>${field.name}_removed<#else>-</#if>" data-bind="{ value: ${databind}Removed().join(',') }" />
</#macro>

<#macro renderFieldValueAsArray field>
[
	<#if (field.value!"") != "">
		<#list field.value?split(",") as value>
	"${value}"<#if value_has_next>,</#if>
		</#list>
	</#if>
]
</#macro>

<script type="text/javascript">//<![CDATA[
require('citeck/components/form/controls/passport', function(PassportsControl) {
	var control = new PassportsControl("${controlId}");
	control.setOptions({
		<#if form.mode == "create" >
            <#if peopleField?? && peopleField.value?? && peopleField.value = "" && params.peopleDefault?? >
            peopleIds: [
            <#list field.control.params.peopleDefault?split(",") as personNodeRef>
                "${personNodeRef}"<#if personNodeRef_has_next>,</#if>
            </#list>
            ],
            <#else>
            peopleIds: <@renderFieldValueAsArray peopleField />,
            </#if>
            peopleIdsOriginal: "",
            passportIdsOriginal: "",
		<#else>
            peopleIdsOriginal: <@renderFieldValueAsArray peopleField />,
            passportIdsOriginal: <@renderFieldValueAsArray passportsField />,
		</#if>
		peopleDialogOptions: {
			model: <@renderOrgstructModelJS params = {
				"searchQuery": "user=true&role=true$default=false"
			} />,
			tree: {
				buttons: {
					"USER": [ "itemSelect" ],
                    "GROUP": [ "itemSelect" ],
					"selected-yes": [ "itemUnselect" ]
				}
			},
			list: {
				buttons: {
					"selected-yes": [ "itemUnselect" ],
				},
			}					
		},
		passportsDestination: "${field.control.params.passportsDestination!msg("passports.path")}",		
	});
})
//]]></script>

<#macro renderTableHeader columns>
	<thead>
		<tr class="yui-dt-first yui-dt-last">
			<#list columns as column>
			<th rowspan="1" colspan="1" class="yui-dt-col-${column.id} <#if column_index == 0>yui-dt-first</#if> <#if !column_has_next>yui-dt-last</#if>">
				<div class="yui-dt-liner">
					<span class="yui-dt-label">${msg(column.header)}</span>
				</div>
			</th>
			</#list>
		</tr>
	</thead>
</#macro>

<#macro renderPassportsTable>
<div class="yui-dt passports-table flat-button">
<table>
	<@renderTableHeader columns = [
		{ "id": "person", "header": "passports.header.person" },
		{ "id": "passport", "header": "passports.header.passport" },
		{ "id": "date", "header": "passports.header.date" },
		{ "id": "actions", "header": "passports.header.actions" }
	] />
	<tbody tabindex="0" class="yui-dt-data">
		<!-- ko foreach: records -->
		<tr class="yui-dt-rec" data-bind="css: {
			'yui-dt-first': $index == 0,
			'yui-dt-last': $index == $root.records().length-1,
			'yui-dt-even': ($index % 2) == 0,
			'yui-dt-odd': ($index % 2) == 1
		}">
		
			<td class="yui-dt-col-person yui-dt-first">
				<div class="yui-dt-liner" data-bind="text: person().displayName"></div>
			</td>
			
			<td class="yui-dt-col-passport">
				<!-- ko ifnot: passport -->
				<div class="yui-dt-liner">${msg("passports.message.no-data")}</div>
				<!-- /ko -->
				<!-- ko if: passport -->
					<!-- ko with: passport -->
						<!-- ko ifnot: canRead -->
				<div class="yui-dt-liner">${msg("passports.message.no-access")}</div>
						<!-- /ko -->
						<!-- ko if: canRead -->
							<!-- ko ifnot: info -->
				<div class="yui-dt-liner">${msg("passports.message.empty-data")}</div>
							<!-- /ko -->
							<!-- ko if: info -->
				<div class="yui-dt-liner" data-bind="text: info"></div>
							<!-- /ko -->
						<!-- /ko -->
					<!-- /ko -->
				<!-- /ko -->
			</td>
			
			<td class="yui-dt-col-date">
				<div class="yui-dt-liner">
					<#if form.mode == "view">
						<!-- ko if: passport -->
							<!-- ko text: passport().viewDate --><!-- /ko -->
						<!-- /ko -->
					<#else/>
						<!-- ko if: passports() && passports().passports().length != 0 -->
							<select data-bind="
								options: passports().passports, 
								optionsText: 'viewDate',
								value: passport">
							</select>
						<!-- /ko -->
					</#if>
				</div>
			</td>
			
			<td class="yui-dt-col-actions yui-dt-last">
				<div class="yui-dt-liner">
				
					<#if form.mode != "view">
					<span class="add" title="${msg("passports.button.add.info")}" data-bind="yuiButton: { type: 'push' }">
						<span class="first-child">
							<button data-bind="click: $root.addPassport.bind($root, $data)">${msg("passports.button.add")}</button>
						</span>
					</span>
					</#if>
					
					<#if form.mode != "view">
					<!-- ko if: passport() && passport().canWrite() -->
					<span class="edit" title="${msg("passports.button.edit.info")}" data-bind="yuiButton: { type: 'push' }">
						<span class="first-child">
							<button data-bind="click: $root.editPassport.bind($root, $data)">${msg("passports.button.edit")}</button>
						</span>
					</span>
					<!-- /ko -->
					</#if>
								
					<!-- ko if: passport() && passport().canRead() -->
					<span class="view" title="${msg("passports.button.view.info")}" data-bind="yuiButton: { type: 'push' }">
						<span class="first-child">
							<button data-bind="click: $root.viewPassport.bind($root, $data)">${msg("passports.button.view")}</button>
						</span>
					</span>
					<!-- /ko -->
					
				</div>
			</td>
			
		</tr>
		<!-- /ko -->
	</tbody>
</table>
</div>
</#macro>

<div id="${controlId}" class="form-field">

	<@renderHiddenInputs peopleField "peopleIds" />
	<@renderHiddenInputs passportsField "passportIds" />

	<#if form.mode == "view">
		<span class="viewmode-label">${field.label?html}:</span>
	<#else/>
	<label class="passports-label">
		${field.label?html}: 
		<#if (passportsField.endpointMandatory!false) || (passportsField.mandatory!false)
			|| (peopleField.endpointMandatory!false) || (peopleField.mandatory!false)>
			<span class="mandatory-indicator">${msg("form.required.fields.marker")}</span>
		</#if>
			
		<#if field.control.params.validationField??>
		<input id="${args.htmlid}_${field.control.params.validationField}" type="hidden" name="${field.control.params.validationField}" data-bind="value: allPassportsPresent" />
		<!-- ko if: records().length != 0 -->
		<span class="passports-validation">
			<!-- ko if: allPassportsPresent -->
				<img src="${url.context}/res/citeck/images/check.png" style="width: 16px; height: 16px" />
				${msg("passports.message.all-passports-present")}
			<!-- /ko -->
			<!-- ko ifnot: allPassportsPresent -->
				<img src="${url.context}/res/citeck/images/warn.png" style="width: 16px; height: 16px" />
				${msg("passports.message.not-all-passports-present")}
			<!-- /ko -->
		</span>
		<!-- /ko -->
		</#if>
	</label>
	</#if>

	<!-- ko if: records().length != 0 -->
	<@renderPassportsTable />	
	
	<#if form.mode != "view" && field.control.params.disclaimerText??>
	<p class="passports-disclaimer">${field.control.params.disclaimerText?html}</p>
	</#if>
	<!-- /ko -->
	
	<#if form.mode == "create">
	<@renderDynamicTreePickerHTML controlId />
	<span data-bind="yuiButton: { type: 'push' }">
		<span class="first-child">
			<button data-bind="click: onSelectPeopleClick">${msg("button.select")}</button>
		</span>
	</span>
	</#if>

</div>
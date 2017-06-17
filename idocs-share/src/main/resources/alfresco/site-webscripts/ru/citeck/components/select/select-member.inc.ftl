<#assign idhtml=args.htmlid/>
<#assign fieldd=field.name/>
<#assign fieldv=field.value?html/>


<#macro getSelectPickerJS elemId number>
	var picker = new SelectorMember("${idhtml}","${fieldHtmlId}", ${number});
	$("#${elemId}").click(function() {
		picker.pickerShow();
	});
</#macro>

<#macro getSelectMemberConteinerHTML conteinerId>
	<label for="${idhtml}-button" style="font-weight: normal; color: rgb(96, 96, 96); margin-bottom: 3px;">${msg("select-member-dialog.form-field.label")}:<span class="mandatory-indicator">*</span></label>
	<div id="${conteinerId}-selectMembers"></div>
    <div>
    	<span class="yui-button yui-push-button">
    		<span class="first-child">
    			<button type="button" width="70px" height="24px" tabindex="0" id="${idhtml}-button">${msg("select-member-dialog.form-field.button")}</button>
    		</span>
   		</span>
   	</div>
	
    <input type="hidden" id="${fieldHtmlId}" name="-" value="${fieldv}">
    <input type="hidden" id="${fieldHtmlId}-added" name="${fieldd}_added" />
	<input type="hidden" id="${fieldHtmlId}-removed" name="${fieldd}_removed" />
</#macro>

<#macro getSelectPickerHTML>
<div id="orgstruct-select-member-container">
	<style type="text/css" media="screen">
		.${idhtml}-picker-body {border: 2px solid #DBDBDB; width: 816px; background-color: #DBDBDB; border-left: 4px solid #DBDBDB;}
		#${idhtml}-tree {overflow: scroll; overflow-x: visible;}
		#${idhtml}-treeview {margin-left: 3px;}
		#${idhtml}-roles {border-left: 3px solid #DBDBDB; border-right: 3px solid #DBDBDB;overflow: scroll; overflow-x: hidden; margin-left: -4px;}
		#${idhtml}-members {overflow: scroll; overflow-x: hidden; margin-left: -4px;}
		.${idhtml}-item {display: inline-block;vertical-align: top; width: 270px; height: 270px; background-color: #F9FBFD;}
		#${idhtml}-tr {display: block;  cursor: pointer; padding: 10px 5px 5px 5px; width: 260px; height: 20px; border-bottom: 1px dashed #6C6C6C; background-color: #fff;}
		#${idhtml}-table {width: inherit;height: inherit;}
		#${idhtml}-tr:hover{color: red;}
		.${idhtml}-picker-body table {border-spacing: 0px;}
		#${idhtml}-listmem {width: 816px;}
		#${idhtml}-listmemItem {display: inline-block; margin-right: 5px; margin-left: 5px; margin-bottom: 2px;}
		#${idhtml}-btnadd {margin-right: 5px; cursor: pointer;}
		#${idhtml}-btndel {margin-left: 5px; cursor: pointer;}
		#${idhtml}-selectMembers {margin-bottom: 3px;}
		#${idhtml}-button {width:70px; height:24px;}
		#${idhtml}-it_name {}
		
	</style>

	<div id="select-member-dialog" class="${idhtml}-picker">
    	<div class="hd" id="${idhtml}-hd">${msg("select-member-dialog.title")}</div>
    	<div class="bd" id="${idhtml}-bd">
    		<div class="${idhtml}-picker-body">
    		
    			<div id="${idhtml}-tree" class="${idhtml}-item">
    				<div id="${idhtml}-treeview"></div>
    			</div>
    			<div id="${idhtml}-roles" class="${idhtml}-item">
    				<table  id="${idhtml}-table" cellpadding="0" cellspacing="0">
  						<tbody>
  						</tbody>
					</table>
    			</div>
    			<div id="${idhtml}-members" class="${idhtml}-item">
    				<table  id="${idhtml}-table" cellpadding="0" cellspacing="0">
  						<tbody>
  						</tbody>
					</table>
    			</div>
			</div>
			<div id="${idhtml}-listmemlabel"><h3>${msg("select-member-dialog.listmem.title")}:</h3></div> 
			<div id="${idhtml}-listmem"></div>   
    	</div>
	</div>
</div>
</#macro>
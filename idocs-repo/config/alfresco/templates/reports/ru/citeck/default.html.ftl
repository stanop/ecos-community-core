<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="ru-ru" xml:lang="ru-ru">
<head>
    <title><#if reportTitle??>${reportTitle}</#if></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <style type="text/css">
        body {
        	text-align: center;
        	font: 9pt Arial, Tahoma, Verdana;
        }
        
    	table.report {
    		border: 1px solid #bbb !important; 
    		border-collapse: collapse !important; 
    		border-spacing: 0 !important; 
    		width: 94%;
    		margin: 10px 6% 10px 4%;
    	}
    	
        table.report td {
        	border: 1px solid #bbb;
        	font: 9pt Arial, Tahoma, Verdana;
        	padding: 5px; 
        	text-align: left;
        }
        
        table.report td.colheader {
        	font-weight: bold;
        	text-align: center;
        	background-color: #ddd;
        }
        
        table.report td.even {
        	background-color: #f9f9f9;
        }
        
        table.report td.integer {
        	text-align: right;
        	width: 1%;
        } 
    </style>
</head>
<body>
	<#if reportTitle??>
    	<h2>${reportTitle}</h2>
    </#if>

	<#if (nodes?size > 0 && reportColumns?? && reportColumns?size > 0)>
		<table class="report" border="1">
			<tr>
				<#list reportColumns as col>
					<td class="colheader"><#if col.title??>${col.title}<#else>${col.attribute}</#if></th> 	
				</#list>
			</tr>
			
			<#assign rowNum = 1>
			<#list reportData as rowData>
				<#if (rowNum % 2) == 1>
					<#assign rowClass = "odd" />
				<#else>
                	<#assign rowClass = "even" />
            	</#if>
				<tr>
					<#list rowData as cellData>
					    <#if cellData.type??>
    						<#if cellData.type == "Integer">
    							<td class="integer ${rowClass}">
    						<#else>
    							<td class="${rowClass}">
    						</#if>
    						
    						<#if cellData.value??>
    						    ${cellData.value}
    						</#if>
    						
    						</td>
                		</#if>
					</#list>
				</tr>
				<#assign rowNum = rowNum + 1>
			</#list>
		</table>
	</#if>
</body>
</html>

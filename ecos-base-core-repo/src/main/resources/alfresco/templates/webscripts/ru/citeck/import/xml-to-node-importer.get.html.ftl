<#escape x as jsonUtils.encodeJSONString(x)>

<#macro printStatus obj>
    <#assign status = obj.properties["xni:status"]>
    <#switch status>
        <#case "New">
            <span style="color: blue; ">${status}</span>
        <#break>
        <#case "Ready">
            <span style="color: blue; ">${status}</span>
        <#break>
        <#case "In progress">
            <span style="color: yellow; ">${status}</span>
        <#break>
        <#case "Complete">
            <span style="color: green; ">${status}</span>
        <#break>
        <#case "Error">
            <span style="color: red; ">${status}</span>
        <#break>
        <#case "Deleting">
            <span style="color: black; ">${status}</span>
        <#break>
    </#switch>
</#macro>

<html>
<head>
    <title>XML to node importer</title>
    <style>
        body {
            font-family: "Lucida Sans Unicode", "Lucida Grande", Sans-Serif;
            background: #fff;
            color: #039;
        }
        h3 {
            font-weight: normal;
        }
        table {
            font-size: 12px;
            margin: 20px;
            width: 95%;
            border-collapse: collapse;
            text-align: left;
        }
        table th {
            font-size: 14px;
            font-weight: normal;
            padding: 10px 8px;
            border-bottom: 2px solid #6678b1;
        }
        table td {
            border-bottom: 1px solid #ccc;
            color: #669;
            padding: 6px 8px;
        }
        table tbody tr:hover td {
            color: #009;
        }
        #loader {
            position: fixed;
            left: 50%;
            top: 50%;
            z-index: 1;
            width: 150px;
            height: 150px;
            margin: -75px 0 0 -75px;
            border: 16px solid #f3f3f3;
            border-radius: 50%;
            border-top: 16px solid #3498db;
            -webkit-animation: spin 2s linear infinite;
            animation: spin 2s linear infinite;
            opacity: 0.5;
        }

        @-webkit-keyframes spin {
            0% { -webkit-transform: rotate(0deg); }
            100% { -webkit-transform: rotate(360deg); }
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .animate-bottom {
            position: relative;
            -webkit-animation-name: animatebottom;
            -webkit-animation-duration: 1s;
            animation-name: animatebottom;
            animation-duration: 1s
        }

        @-webkit-keyframes animatebottom {
            from { bottom:-100px; opacity:0 }
            to { bottom:0px; opacity:1 }
        }

        @keyframes animatebottom {
            from{ bottom:-100px; opacity:0 }
            to{ bottom:0; opacity:1 }
        }
        #loadText {
            display: none;
            text-align: center;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 1;
            padding-top: 100px;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgb(0,0,0);
            background-color: rgba(0,0,0,0.4);
        }

        .modal-content {
            position: relative;
            background-color: #fefefe;
            margin: auto;
            padding: 0;
            border: 1px solid #888;
            width: 80%;
            box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19);
            -webkit-animation-name: animatetop;
            -webkit-animation-duration: 0.4s;
            animation-name: animatetop;
            animation-duration: 0.4s
        }

        @-webkit-keyframes animatetop {
            from {top:-300px; opacity:0}
            to {top:0; opacity:1}
        }

        @keyframes animatetop {
            from {top:-300px; opacity:0}
            to {top:0; opacity:1}
        }

        .close {
            color: white;
            float: right;
            font-size: 28px;
            font-weight: bold;
        }

        .close:hover,
        .close:focus {
            color: #000;
            text-decoration: none;
            cursor: pointer;
        }

        .modal-header {
            padding: 2px 16px;
            background-color: #3498db;
            color: white;
        }

        .modal-body {padding: 2px 16px;}

        .modal-footer {
            padding: 2px 16px;
            background-color: #3498db;
            color: white;
        }
        .button {
            background-color: #3498db;
            border: none;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }
        #inputForFilter {
            width: 100%;
            font-size: 16px;
            padding: 12px 20px 12px 20px;
            border: 1px solid #6678b1;
        }

        .input::-webkit-input-placeholder       {opacity: 1; transition: opacity 0.3s ease;}
        .input::-moz-placeholder                {opacity: 1; transition: opacity 0.3s ease;}
        .input:-moz-placeholder                 {opacity: 1; transition: opacity 0.3s ease;}
        .input:-ms-input-placeholder            {opacity: 1; transition: opacity 0.3s ease;}
        .input:focus::-webkit-input-placeholder {opacity: 0; transition: opacity 0.3s ease;}
        .input:focus::-moz-placeholder          {opacity: 0; transition: opacity 0.3s ease;}
        .input:focus:-moz-placeholder           {opacity: 0; transition: opacity 0.3s ease;}
        .input:focus:-ms-input-placeholder      {opacity: 0; transition: opacity 0.3s ease;}
    </style>

    <script>
        var refreshLoadingDescriptionTimer;
        const CONFIRM_DELETION_KEY = "I confirm the deletion";

        function start() {
            document.getElementById("loader").style.display = "block";
            var loadingText = '<h2>Page is loading...</h2>';
            setLoadingText(loadingText);
            document.getElementById("loadText").style.display = "block";
            initializationPage();
        }

        function initializationPage() {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", '${url.context}' + "/s/citeck/node?nodeRef=workspace://SpacesStore/xni-parser-status" +
                    "&props=xni:prsStatus,xni:activeParsingDescription", true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState != 4) return;
                if (xhr.status == 200) {
                    var processStatus = JSON.parse(xhr.responseText);
                    var status = processStatus.props["xni:prsStatus"];
                    if (status == "Executing") {
                        drawExecutingPage();
                    }
                    if (status == "Wait") {
                        drawWaitingPage();
                    }
                }
                if (xhr.status == 204) {
                    return xhr.statusText;
                }
            };
            xhr.send();
        }

        function drawExecutingPage() {
            loadingStart();
        }

        function drawWaitingPage() {
            loadingStop();
        }

        function loadingStart() {
            refreshLoadingDescriptionTimer = setInterval(function() {
                var xhr = new XMLHttpRequest();
                xhr.open("GET", '${url.context}' + "/s/citeck/node?nodeRef=workspace://SpacesStore/xni-parser-status" +
                        "&props=xni:prsStatus,xni:activeParsingDescription", true);
                xhr.onreadystatechange = function () {
                    if (xhr.readyState != 4) return;
                    if (xhr.status == 200) {
                        var processStatus = JSON.parse(xhr.responseText);
                        var description = processStatus.props["xni:activeParsingDescription"];
                        var displayText = '<h2>Executing in progress!</h2>' +
                                '<p>Please, check errors in alfresco.log<p>';
                        displayText = displayText.concat('\n').concat(description);
                        setLoadingText(displayText);

                        var status = processStatus.props["xni:prsStatus"];
                        if (status == "Wait") {
                            if (refreshLoadingDescriptionTimer) {
                                clearInterval(refreshLoadingDescriptionTimer);
                            }
                            popUpMessage('<h2>Processing data finished</h2>',
                                    '<p>Please, check errors in alfresco.log</p>');
                        }
                    }
                    if (xhr.status == 204) {
                        setLoadingText(xhr.statusText);
                    }
                };
                xhr.send();
            }, 3000);

            document.getElementById("xniTable").style.display = "none";
            document.getElementById("inputForFilter").style.display = "none";
            document.getElementById("buttons").style.display = "none";
            document.getElementById("loadText").style.display = "block";
            document.getElementById("loader").style.display = "block";
        }

        function loadingStop() {
            document.getElementById("xniTable").style.display = "";
            document.getElementById("inputForFilter").style.display = "";
            document.getElementById("buttons").style.display = "";
            document.getElementById("loadText").style.display = "none";
            document.getElementById("loader").style.display = "none";
        }

        function setLoadingText(message) {
            if (message) document.getElementById("loadText").innerHTML = message;
        }

        function popUpMessage(topMsg, bodyMsg) {
            var modalContent = document.getElementById("modalContent");
            var topMessage = '<span class="close">&times;</span>';
            topMessage = topMessage.concat(topMsg);
            modalContent.innerHTML = topMessage;

            var modalBody = document.getElementById("modalBody");
            modalBody.innerHTML = bodyMsg;

            var modal = document.getElementById('mModal');
            var span = document.getElementsByClassName("close")[0];

            modal.style.display = "block";

            span.onclick = function() {
                modal.style.display = "none";
                location.reload()
            };

            window.onclick = function(event) {
                if (event.target == modal) {
                    modal.style.display = "none";
                    location.reload();
                }
            };
        }


        function selectAll() {
            var elements = document.getElementsByName("checkbox");
            for (var i = 0; i < elements.length; i ++) {
                var element = elements[i];
                var tdTable = element.parentElement.parentElement.parentElement;
                if (!element.checked && !element.readOnly && tdTable.style.display != "none") {
                    element.checked = true;
                }
            }
        }

        function deSelectAll() {
            var elements = document.getElementsByName("checkbox");
            for (var i = 0; i < elements.length; i ++) {
                var element = elements[i];
                if (element.checked && !element.readOnly) {
                    element.checked = false;
                }
            }
        }

        function filter() {
            var input, filter, table, tr, td, i;
            input = document.getElementById("inputForFilter");
            filter = input.value.toUpperCase();
            table = document.getElementById("xniTable");
            tr = table.getElementsByTagName("tr");
            for (i = 0; i < tr.length; i++) {
                td = tr[i].getElementsByTagName("td")[3];
                if (td) {
                    if (td.innerHTML.toUpperCase().indexOf(filter) > -1) {
                        tr[i].style.display = "";
                    } else {
                        tr[i].style.display = "none";
                    }
                }
            }
        }

        function executeSelected() {
            processing("execute");
        }

        function deleteSelected() {
            processing("delete");
        }

        function processing(type) {
            var formData = new FormData();
            var elements = document.getElementsByName("checkbox");
            var formDataIsEmpty = true;
            var invalidElements = [];

            switch (type+"") {
                case "execute":
                    for (i = 0; i < elements.length; i ++) {
                        if (elements[i].checked) {
                            var currentStatus = elements[i].getAttribute("xniStatus");
                            currentStatus = currentStatus + "";
                            if (currentStatus == "New" || currentStatus == "Ready") {
                                formData.append("nodeRef", elements[i].id);
                                formDataIsEmpty = false;
                            } else {
                                invalidElements.push(elements[i].id)
                                formDataIsEmpty = false;
                            }
                        }
                    }

                    if (formDataIsEmpty) {
                        popUpMessage('<h2>Cannot start processing</h2>', '<p>Select some data to start processing</p>');
                        return;
                    }

                    if (invalidElements.length > 0) {
                        popUpMessage(
                            '<h2>Cannot start processing</h2>',
                            '<p>Invalid selected data found. Importing data is only available for "New" and "Ready" statuses</p>'
                        );
                        return;
                    }

                    executeData(formData);
                    break;
                case "delete":
                    for (var i = 0; i < elements.length; i ++) {
                        if (elements[i].checked) {
                            formData.append("nodeRef", elements[i].id);
                            formDataIsEmpty = false;
                        }
                    }

                    if (formDataIsEmpty) {
                        popUpMessage('<h2>Cannot start processing</h2>', '<p>Select some data to start processing</p>');
                        return;
                    }

                    var confirmDelete = prompt("Warning! All selected data will be removed. To confirm the deletion, " +
                            "enter: \"" + CONFIRM_DELETION_KEY + "\"");

                    if (confirmDelete === CONFIRM_DELETION_KEY) {
                        deleteData(formData)
                    } else {
                        popUpMessage('<h2>Deleting not started</h2>', '<p>You did not confirm deletion of data</p>');
                        return;
                    }
                    break;
                default:
                    alert("Incorrect processing type");
            }

            loadingStart();
        }

        function executeData(data) {
            var xhr = new XMLHttpRequest();
            xhr.open("POST", '${url.service}' + "execute/", true);
            xhr.send(data);
        }

        function deleteData(data) {
            var xhr = new XMLHttpRequest();
            xhr.open("DELETE", '${url.service}' + "delete/", true);
            xhr.send(data);
        }
    </script>

</head>
<body onload="start()">
<h3 align="center">XML to node importer</h3>
<p>On this moment import data available only for 'New' and 'Ready' status of data objects.</p>
<div style="display:none;" id="loadText" class="animate-bottom" align="center"></div>
<div id="loader" class="loader" style="display: none"></div>

<#if xmlData?size == 0>
<p>There are no XNI data available</p>
<#else>

<div id="mModal" class="modal">
    <div class="modal-content">
        <div id="modalContent" class="modal-header"></div>
        <div id="modalBody" class="modal-body"></div>
        <div class="modal-footer"></div>
    </div>
</div>

<div id ="buttons" style="display: none">
    <input class="button" type="button" value="Execute selected" onclick="executeSelected()">
    <input class="button" type="button" value="Delete selected" onclick="deleteSelected()">
    <input class="button" type="button" value="Select all" onclick="selectAll()">
    <input class="button" type="button" value="Deselect all" onclick="deSelectAll()">
</div>

<input class="input" id="inputForFilter" onkeyup="filter()" placeholder="Search for tag..."
       title="Type in a tag" style="color: #6678b1">

<table id="xniTable" class="moduletable" style="display: none">
    <thead>
    <tr>
        <th>Select</th>
        <th>Name</th>
        <th>NodeRef</th>
        <th>Tag</th>
        <th>Status</th>
    </tr>
    </thead>
    <tbody>
        <#list xmlData as obj>
        <tr>
            <td>
                <label>
                    <input type="checkbox" name="checkbox" id="${obj.nodeRef}" xniStatus="${obj.properties["xni:status"]}"/>
                </label>
            </td>
            <td>${obj.properties["cm:name"]}</td>
            <td>${obj.nodeRef}</td>
            <td>${obj.properties["xni:tag"]!""}</td>
            <td><@printStatus obj = obj/></td>
        </tr>
        </#list>
    </tbody>
</table>
</#if>
</body>
</html>
</#escape>
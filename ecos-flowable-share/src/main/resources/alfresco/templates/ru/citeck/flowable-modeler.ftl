<#include "/org/alfresco/include/alfresco-template.ftl" />

<@templateHeader>
	<meta http-equiv="Cache-Control" content="private" >

	   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/citeck/lib/flowable-modeler.css" />
</@>

<@templateBody>
	<div id="alf-hd">
        <#include "/ru/citeck/include/header.ftl" />
	</div>
	<div id="bd">
		<div id="flowable-modeler">
            <script type="text/javascript">//<![CDATA[

                var searchUrl = Alfresco.constants.PROXY_URI + '/citeck/global-properties?name=flowable.modeler.url';
                Alfresco.util.Ajax.request({
                    url: searchUrl,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            var serverResponse = response.serverResponse ? response.serverResponse : {};
                            if (serverResponse.status === 200 && serverResponse.responseText) {
                                var responseData = eval('(' + serverResponse.responseText + ')');
                                if (responseData.data && responseData.data['flowable.modeler.url']) {
                                    var modeler = document.createElement('iframe');
                                    modeler.setAttribute('src', responseData.data['flowable.modeler.url']);
                                    modeler.setAttribute('id', 'flowable-modeler-iframe');
                                    $('#flowable-modeler').append(modeler);

                                    $('#flowable-modeler-iframe').load(function() {
                                        var stl = '<style type="text/css">.ng-scope .navbar {position: inherit; margin-bottom: 0;} .ng-scope .navbar-nav > li > a {padding: 10px 20px;} .ng-scope .wrapper.full {padding: 0;} .ng-scope .navbar-header .landing-logo{width: 0;}</style>';
                                        $(this).contents().find('head').append(stl);
                                    });

                                } else {
                                    Alfresco.util.PopupManager.displayPrompt({
                                        text: "Missing 'flowable.modeler.url' in global properties"
                                    });
                                }
                            }
                        }
                    },
                    failureCallback: {
                        scope: this,
                        fn: function(response) {
                            Alfresco.util.PopupManager.displayPrompt({
                                text: "Flowable modeler not found"
                            });
                        }
                    },
                    execScripts: true
                });
            //]]></script>
        </div>
	</div>

</@>

<@templateFooter>
	<div id="alf-ft">
		<@region id="footer" scope="global" />
	</div>
</@>
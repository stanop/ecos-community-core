<@markup id="css" >
    <@link href="${url.context}/res/css/citeck/lib/flowable-modeler.css" group="console"/>
</@>

<@markup id="html">
    <div id="flowable-modeler">
        <script type="text/javascript">//<![CDATA[

            var searchUrl = Alfresco.constants.PROXY_URI + '/citeck/global-properties?name=flowable.rest-api.url';
            Alfresco.util.Ajax.request({
                url: searchUrl,
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var serverResponse = response.serverResponse ? response.serverResponse : {};
                        if (serverResponse.status === 200 && serverResponse.responseText) {
                            var responseData = eval('(' + serverResponse.responseText + ')');
                            if (responseData.data && responseData.data['flowable.rest-api.url']) {
                                var modeler = document.createElement('iframe');
                                modeler.setAttribute('src', responseData.data['flowable.rest-api.url']);
                                $('#flowable-modeler').append(modeler);
                            } else {
                                Alfresco.util.PopupManager.displayPrompt({
                                    text: "Missing 'flowable.rest-api.url' in global properties"
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
</@>
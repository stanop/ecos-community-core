<@markup id="html">

<div id="share-javascript-console" style="width:800px">
    <div><textarea id="share-js-code" type="text" style="width:100%; height:300px"></textarea></div>
    <div><input id="share-js-submit" type="button" name="submit" value="Submit"/> or Ctrl + Enter</div>
    <div id="share-js-output" style="margin-top: 10px"></div>
</div>

<script type="text/javascript">
    require(['jquery', 'js/citeck/lib/json-viewer/json-viewer'], function () {

        var submitButton = $('#share-js-submit');
        var codeTextArea = $('#share-js-code');

        var buttonEnabled = true;

        var executeScript = function () {

            if (!buttonEnabled) {
                return
            }

            Alfresco.util.Ajax.jsonPost({
                url: '/share/service/ru/citeck/utils/execute-script',
                dataObj: {
                    script: codeTextArea[0].value
                },
                successCallback: {
                    scope: this,
                    fn: function (response) {
                        $('#share-js-output').jsonview(response.json);
                        submitButton.css("background-color", "#90F094");
                        submitButton.prop('disabled', false);
                        buttonEnabled = true
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function (response) {
                        $('#share-js-output').jsonview({
                            error: response.serverResponse.responseText
                        });
                        submitButton.css("background-color", "#FFB996");
                        submitButton.prop('disabled', false);
                        buttonEnabled = true
                    }
                }
            });
            buttonEnabled = false
        };

        submitButton.on('click', executeScript);
        codeTextArea.keypress(function (event) {
            var keyCode = (event.which ? event.which : event.keyCode);
            if (keyCode === 10 || keyCode === 13 && event.ctrlKey) {
                executeScript();
                return false;
            }
            return true;
        });
    });
</script>

</@>
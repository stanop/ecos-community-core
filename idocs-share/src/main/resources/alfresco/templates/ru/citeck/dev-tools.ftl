<html>
<head>
    <title><#if page.titleId??>${msg(page.titleId)}<#else>${page.title!}</#if></title>
    <script type="text/javascript" src="/share/res/js/yui-common.js"></script>
    <script type="text/javascript" src="/share/res/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="/share/res/jquery/jquery.js"></script>
    <script type="text/javascript" src="/share/res/js/alfresco.js"></script>
    <script>
        function resetServicesCache() {
            var form = $('#reset-repo-cache-form');

            var button = form.find('.submit-button');
            button.css("background-color", "yellow");
            button.prop('disabled', true);

            var formData = {};
            form.find('input').each(function(idx, inp) {
                if (!inp.name || inp.name == '-') {
                    return;
                }
                if (inp.type == 'checkbox') {
                    formData[inp.name] = inp.checked;
                } else {
                    formData[inp.name] = inp.value;
                }
            });

            Alfresco.util.Ajax.jsonPost({
                url: '/share/proxy/alfresco/citeck/dev/reset-cache',
                dataObj: formData,
                successCallback: {
                    scope: this,
                    fn: function() {
                        button.css("background-color", "#90F094");
                        button.prop('disabled', false);
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function() {
                        button.css("background-color", "#FF757C");
                        button.prop('disabled', false);
                    }
                }
            });
        }

        function updateForceNewFormsBtn() {
            var isEnabled = localStorage.forceEnableNewForms == 'true';
            var btn = $('#force-new-forms-btn');
            if (isEnabled) {
                btn.addClass('toogle_btn_enabled');
                btn.removeClass('toogle_btn_disabled');
            } else {
                btn.addClass('toogle_btn_disabled');
                btn.removeClass('toogle_btn_enabled');
            }
        }

        function toggleForceNewForms() {
            localStorage.forceEnableNewForms = localStorage.forceEnableNewForms != 'true';
            updateForceNewFormsBtn();
        }
    </script>
    <style>
        iframe {
             border: none;
             height: 200px;
        }
        iframe:hover {
            outline: 1px solid lightgray;
        }
        .toogle_btn_enabled {
            background-color: #bfe874;
        }
        .toogle_btn_disabled {
            background-color: #dddddd;
        }
    </style>
</head>
<body>

    <h2>Repository Tools</h2>

    <div>
        <form action="/alfresco/service/index" method="post" target="repo-webscripts" style="display: inline-block">
            <input type="hidden" name="reset" value="on" />
            <input type="submit" name="submit" value="Refresh Web Scripts" />
        </form>
        <form id="reset-repo-cache-form" style="display: inline-block">
            <div style="margin-left: 10px; margin-right: 10px; display: inline-block; width: 350px; vertical-align: middle">
                <label><input type="checkbox" name="journals" />Journals</label>
                <label><input type="checkbox" name="views" />Views</label>
                <label><input type="checkbox" name="invariants" />Invariants</label>
                <label><input type="checkbox" name="case-templates" />Case templates</label>
                <label><input type="checkbox" name="fields-perm-matrix" />Fields permissions matrix</label>
                <label><input type="checkbox" name="perm-matrix" />Permissions matrix</label>
                <label><input type="checkbox" name="cardlets" />Cardlets</label>
                <label><input type="checkbox" name="forms" />Forms</label>
            </div>
            <input class="submit-button" type="button" value="Reload Selected" onclick="resetServicesCache()" />
        </form>
        <div style="margin-left: 20px; display: inline-block">
            <input id="force-new-forms-btn" type="button" value="Force New Forms" onclick="toggleForceNewForms()" />
        </div>
    </div>
    <iframe name="repo-webscripts" width="500px"></iframe>
    <iframe name="repo-js-debugger" src="/share/proxy/alfresco/api/javascript/debugger"></iframe>
    <iframe name="repo-modules" src="/share/proxy/alfresco/modules/info.html" width="500px"></iframe>

    <h2>Share Tools</h2>
    
    <form action="${url.context}/page/index" method="post" target="share-webscripts">
        <input type="hidden" name="reset" value="on" />
        <input type="submit" name="submit" value="Refresh Web Scripts" />
    </form>
    <iframe name="share-webscripts" width="500px"></iframe>
    <iframe name="share-js-debugger" src="${url.context}/page/api/javascript/debugger"></iframe>
    <iframe name="share-modules" src="${url.context}/page/modules/info.html" width="500px"></iframe>
    
    <form action="${url.context}/page/caches/dependency/clear" method="post" target="share-caches">
        <input type="submit" name="submit" value="Clear Dependency Caches" />
    </form>
    <form action="${url.context}/page/citeck/cache/clear?updateCacheBust=true" method="post" target="share-caches">
        <input type="submit" name="submit" value="Update cache bust" />
    </form>
    <iframe name="share-caches"></iframe>

    <iframe name="share-surfbug" src="${url.context}/page/surfBugStatus"></iframe>

    <script>
        updateForceNewFormsBtn();
    </script>

</body>
</html>
<#assign id=args.htmlid?js_string>

<script>
    function copyToClipboard() {
        var documentProperty = document.querySelector('.document-clipboard-capture-property');
        documentProperty.select();
        try {
            document.execCommand('copy');
        } catch(err) {
            console.log('Oops, unable to copy');
        }
        window.getSelection().removeAllRanges();
    }

    $(document).ready(function() {
        var input = $(".document-clipboard-capture-property");
        var newWidth = input.val().length * 8;
        input.css({width:newWidth});
    });

    $(document).ready(function() {
        $(".yui-button.yui-push-button").hover(function () {
            $(this).toggleClass("yui-button-hover");
            $(this).toggleClass("yui-push-button-hover");
        });
    })
</script>

<div id="${id}" class="document-clipboard-capture document-details-panel">
    <h2 id="${id}-heading" class="thin dark alfresco-twister">
        <div class="form-field suggested-actions">
            <span>${msg("${args.title}")}</span>
            <input type="text" class="panel-body document-clipboard-capture-property" value="${property}" readonly title="" onclick="this.select()"/>
            <span class="yui-button yui-push-button">
                <span class="first-child">
                    <button type="button" tabindex="0" onclick="copyToClipboard()" name="-">${msg("copy-to-clipboard")}</button>
                </span>
            </span>
        </div>
    </h2>
</div>
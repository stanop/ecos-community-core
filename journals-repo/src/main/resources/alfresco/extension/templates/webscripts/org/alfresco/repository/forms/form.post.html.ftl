<script type="text/javascript">//<![CDATA[
require(['citeck/components/form/multipart-form-support'], function () {
    if (window.parent && window.parent.MultipartFormSupport) {
        window.parent.MultipartFormSupport.onSubmitSuccess(
        <#include "form.post.json.ftl" />
                , window.frameElement.id);
    } else {
        alert("Erroneous multipart/form-data processing conditions");
    }
});
//]]></script>
<#assign params = viewScope.region.params!{} />

<#-- property: field property name, for example "ecos:photo". Comes from view's field prop value: <field prop="ecos:photo"> ... </field> -->
<#assign property = viewScope.field.attribute!"" />
<#assign propertyU = property?replace(":", "_") />
<#assign imgId = propertyU + "-image-id" />


<#assign img_id="user-groups-list-span" + propertyU />

<#if params.width?has_content>
    <#assign width = "width=" + params.width?string />
<#else>
    <#assign width = "" />
</#if>

<#if params.height?has_content>
    <#assign height = "height=" + params.height?string />
<#else>
    <#assign height = "" />
</#if>

<#if params.title?has_content>
    <#assign title = params.title?string?replace("'", "&#39;")?replace("\"", "&#34;") />
<#else>
    <#assign title = "" />
</#if>

<div class="img-cover" id="${imgId}--cover">
    <!-- ko if: empty -->
    <div class="img-back-container"
         title="${title}"
         id="${imgId}"
         style="background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAMAAACahl6sAAACvlBMVEX///8AAAAyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///////////////8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///////8yWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXL///////8yWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXL///8yWXIyWXIyWXIyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///8yWXL///8yWXIyWXIyWXIxWHEyWXL///8yWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXIyWXL///+BEgC3AAAA6XRSTlMAABRFa4+x0tzb3cuuiWU8DyhdjL3Z7f376de2hlIeA0OO0PjJgTYqhXEbQaz68Js0R8GnMS/8mgtqPntcEa+SAifTuhM7IfYp5yM5JM7Us7B1SzMWBH1Q998KqTi7hP7uFaR4HPUFd9GmMOsOfsUZ2k9atIu39NU3JWF0SlGAY2RMaVY/GgzGyKE9B6BUyqNC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')"
    ></div>
    <!-- /ko -->

    <!-- ko ifnot: empty -->
    <!-- ko foreach: multipleValues -->
    <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
    <!-- ko if: $parent.getValueTitle($data) -->
    <div class="img-back-container"
         title="${title}"
         id="${imgId}"
         data-bind="attr: { style: 'background-image: url(' + window.location.origin + '/share/proxy/alfresco/api/node/content;${property}/workspace/SpacesStore/'+ $parent.getValueTitle($data) +'/image.jpg);' }"
    ></div>
    <!-- /ko -->
    <!-- /ko -->
    <!-- /ko -->
    <!-- /ko -->
</div>

<#-- show uploaded image -->
<script type="text/javascript">//<![CDATA[
(function() {
    YAHOO.Bubbling.on('file-uploaded-${propertyU}', function(layer, args) {
        var reader = new FileReader();
        reader.addEventListener("load", function(event) {
            var imgDiv = document.querySelector('#${imgId}');
            var imgCover = imgDiv.parentNode;
            var imgCoverParent = imgCover.parentNode;

            imgCoverParent.removeChild(imgCover);
            $(imgCoverParent).append('<div class="img-cover" id="${imgId}--cover"><div class="img-back-container" id="${imgId}" style="background-image: url('+ event.target.result +')"></div></div>')
        });
        reader.readAsDataURL(args[1]);
    });
})();
//]]></script>

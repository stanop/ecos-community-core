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

<div class="img-cover" id="${imgId}--cover">
    <!-- ko foreach: multipleValues -->
    <!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
    <!-- ko if: $parent.getValueTitle($data) -->
    <div class="img-back-container" id="${imgId}" data-bind="attr: { style: 'background-image: url(' + window.location.origin + '/share/proxy/alfresco/api/node/content;${property}/workspace/SpacesStore/'+ $parent.getValueTitle($data) +'/image.jpg);' }"></div>
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
            var img = $('#${imgId}');
            var parSpan = img.parent();
            var par = parSpan.parent();
            parSpan.remove();
            par.append('<div class="img-cover" id="${imgId}--cover"><div class="img-back-container" id="${imgId}" style="background-image: url('+ event.target.result +')"></div></div>')
        });
        reader.readAsDataURL(args[1]);
    });
})();
//]]></script>

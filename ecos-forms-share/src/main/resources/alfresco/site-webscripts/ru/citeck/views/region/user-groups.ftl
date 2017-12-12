<#assign params = viewScope.region.params!{} />

<#-- property: field property name, for example "ecos:photo". Comes from view's field prop value: <field prop="ecos:photo"> ... </field> -->
<#assign property = params.property!viewScope.field.attribute!"" />

<#assign span_id="user-groups-list-span"+property?replace(":", "_") />

<!-- ko foreach: multipleValues -->
<!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
<span id="${span_id}">
    <span id=${span_id + "_data"} data-bind="text: $parent.getValueTitle($data)" ></span>
    <script type="text/javascript">//<![CDATA[
    (function() {
        var vTimer = setInterval(function () {
            var uName = $('#${span_id + "_data"}').text();
            if (uName) {
                clearInterval(vTimer);
                var url = Alfresco.constants.PROXY_URI + "/api/people/"+ uName +"?groups=true";

                Alfresco.util.Ajax.request({
                    url: url,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            if (response.json) {
                                // console.log(response.json["groups"]);
                                var groups = response.json["groups"].map(item => item.displayName).join(', ');
                                $('#${span_id}').text(groups);
                            }
                        }
                    }, failureCallback: { scope: this, fn: function(response) {} }, execScripts: true
                });
            }
        }, 200)
    })()
    //]]></script>
</span>
<!-- /ko -->
<!-- /ko -->

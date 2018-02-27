<#assign params = viewScope.region.params!{} />

<#-- property: field property name, for example "ecos:photo". Comes from view's field prop value: <field prop="ecos:photo"> ... </field> -->
<#assign property = params.property!viewScope.field.attribute!"" />

<#assign span_id="user-groups-list-span"+property?replace(":", "_") />

<!-- ko foreach: multipleValues -->
<!-- ko ifnot: $data instanceof koutils.koclass("invariants.Node") -->
<span id="${span_id}">
    <ol></ol>
    <span id=${span_id + "_data"} data-bind="text: $parent.getValueTitle($data)" style="display: none;"></span>
    <script type="text/javascript">//<![CDATA[
    (function() {
        var vTimer = setInterval(function () {
            var uName = $('#${span_id + "_data"}').text();
            if (uName) {
                clearInterval(vTimer);
                var url = Alfresco.constants.PROXY_URI + "/api/people/"+ uName +"?groups=true";
                $('#${span_id + "_data"}').remove();
                $('#user-groups-list-spancm_userName').closest('.form-field').css('width','initial');

                Alfresco.util.Ajax.request({
                    url: url,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            if (response.json) {
                                response.json["groups"].map(function (item) {
                                    $('#${span_id}>ol').append('<li>'+item.displayName+'</li>');
                                })
                            }
                        }
                    },
                    failureCallback: { scope: this, fn: function(response) {} },
                    execScripts: true
                });
            };
        }, 200);
    })();
    //]]></script>
</span>
<!-- /ko -->
<!-- /ko -->

<#assign person_edit_btn_id="person-edit-btn" />
<#assign form_container_id="user-profile-view-inner-container" />

<#assign idxAdmin = user?index_of("GROUP_ALFRESCO_ADMINISTRATORS") />

<#if (viewScope.view.mode == "view") && (idxAdmin > 0) >
<div style="float: right;" class="form-field">

    <button id="person-edit-btn" >Редактировать</button>
    <script type="text/javascript">//<![CDATA[
    (function() {
        setTimeout(function () {
            var btn = YAHOO.util.Dom.get("person-edit-btn");
            var container = $(".${form_container_id}");
            var itemId = $(container).attr('data-itemId');
            var formId = $(container).attr('data-formId');
            var listId = $(container).attr('data-listId');
            btn.onclick = function () {
                // clear junk created by onViewItem
                YAHOO.Bubbling.fire("clearViewJunk");
                var viewItem = new Citeck.forms.showViewInplaced(itemId, formId, function () {}, {listId: listId, mode: 'edit'});
            }
        }, 0);
    })();
    //]]></script>
</div>
</#if>
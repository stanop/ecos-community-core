import React from 'react';
import { connect } from 'react-redux';

function getHintPropertyByCurrentUser(){
    Alfresco.util.Ajax.jsonGet({
        url: Alfresco.constants.PROXY_URI + "citeck/search/query",
        dataObj: {
            query: '=cm:userName:"' + Alfresco.constants.USERNAME + '"',
            schema: JSON.stringify({attributes:{'org:showHints':''}})
        },
        successCallback: {
            scope: this,
            fn: function(response) {
                var showHintValue = response.json.results[0].attributes["org:showHints"];

                var element = document.getElementById("HEADER_HINT_ICON");



                if (showHintValue==="No"){
                    element.style.backgroundImage = 'url(/share/res/components/images/header/dialog_image_off.png)';
                    element.title = "Hints is disabled";
                } else {
                    element.style.backgroundImage = 'url(/share/res/components/images/header/dialog_image.png)';
                    element.title = "Hint is enabled";
                }
            }
        }
    });
}

const SwitchHintMenu = ({}) => {
    let imgIcon =(
            <div className='show-hint'>
                <div id="HEADER_HINT_ICON"  class="headHintIcon" />
            </div>
        );

    let obj = (<div id='HEADER_USER_MENU' className='alfresco-header-showHint'>{imgIcon}</div>)

    getHintPropertyByCurrentUser();

    return obj;
};

export default (SwitchHintMenu);
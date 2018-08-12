import React from "react";
import "xstyle!./node-header.css";

export default function NodeHeader(props) {

    let modifier = props.info.modifier;


    return <div id="{htmlid}">
        <div id="{htmlid}_x0023_default">

            <div className="node-header">
                <div className="node-info">
                    <img src="/share/res/components/images/filetypes/{props.fileExtension}-file-48.png"
                        /*onerror="this.src='/share/res/components/images/filetypes/generic-file-48.png'"*/
                         title="Договор №6" className="node-thumbnail" width="48"/>
                    <h1 className="thin dark">
                        Договор №6<span id="document-version" className="document-version">{props.info.version}</span>
                    </h1>
                    <div>
                        {createModifierInfo(props.info)}
                        <span id="{htmlid}_x0023_default-favourite" className="item item-separator"/>
                        <span id="{htmlid}_x0023_default-like" className="item item-separator"/>
                        <span className="item item-separator item-social">
                                <a href="#" name="@commentNode"
                                   rel="workspace://SpacesStore/33a79124-ccb9-481c-a213-bd1372abb9b7"
                                   className="theme-color-1 comment {htmlid}_x0023_default"
                                   title="Прокомментировать этот документ">Комментировать</a>
                            </span>
                        <span id="{htmlid}_x0023_default-quickshare" className="item item-separator"/>
                    </div>
                </div>

                <div className="node-action">
                </div>

                <div className="clear"/>
            </div>
        </div>
    </div>;
}

function createModifierInfo(info) {

    let modifier = info.modifier;

    return <span className="item-modifier">
        Изменено пользователем <a href={`/share/page/user/${modifier.userName}/profile`}
                                className="theme-color-1">Administrator</a> в <span
        id='{htmlid}-modifyDate'>2018-08-12T03:33:49.171Z</span>
                    </span>;

}
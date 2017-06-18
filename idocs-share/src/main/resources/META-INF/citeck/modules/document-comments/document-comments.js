/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
   var $html = Alfresco.util.encodeHTML,
      $userProfileLink = Alfresco.util.userProfileLink,
      $userAvatar = Alfresco.Share.userAvatar;

if (Alfresco.CommentsList) {
        
    Alfresco.CommentsList.prototype.renderCellComment= function CommentsList_renderCellComment(elCell, oRecord, oColumn, oData)
    {
         // todo: Move this to use js templating when we have it
         var data = oRecord.getData(),
            html = '',
            rowId = this.id + '-' + oRecord.getId(),
            permissions = data.permissions;

         // Display comment
         html += '<div id="' + rowId + '-comment-container" class="comment-details">';
         html += '   <div class="icon">' + $userAvatar(data.author.username) + '</div>';
         html += '   <div class="details">';
         html += '      <span class="info">';
         html += $userProfileLink(data.author.username, data.author.firstName + ' ' + data.author.lastName, 'class="theme-color-1"') + ' ';
         html += '      </span>';
         html += '      <span class="info">';
         html += this.msg("header.modificationDate") +': '+ Alfresco.util.fromISO8601(data.modifiedOnISO).toString('dd.MM.yyyy HH:mm:ss') + '<br/>';
         html += '      </span>';
         html += '      <span class="comment-actions">';
         if (permissions["edit"])
         {
            html += '       <a href="#" name=".onEditCommentClick" rel="' + oRecord.getId() + '" title="' + this.msg("link.editComment") + '" class="' + this.id + ' edit-comment">&nbsp;</a>';
         }
         if (permissions["delete"])
         {
            html += '       <a href="#" name=".onConfirmDeleteCommentClick" rel="' + oRecord.getId() + '" title="' + this.msg("link.deleteComment") + '" class="' + this.id + ' delete-comment">&nbsp;</a>';
         }
         html += '      </span>';
         html += '      <div class="info"><i>' + this.msg("header.version")+": "+(data.description || "") + '</i></div>';
         html += '      <div class="comment-content">' + (data.content || "") + '</div>';
         html += '   </div>';
         html += '   <div class="clear"></div>';
         html += '</div>';
         html += '<div id="' + rowId + '-form-container" class="comment-form hidden">';
         html += '   &nbsp;<!-- EMPTY SPACE FOR FLOATING COMMENT FORM -->';
         html += '</div>';

         // Note! we will initialize form when somebody clicks edit
         elCell.innerHTML = html;
    };
}
})();
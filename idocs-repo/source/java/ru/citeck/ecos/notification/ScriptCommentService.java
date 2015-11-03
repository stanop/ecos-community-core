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
package ru.citeck.ecos.notification;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import ru.citeck.ecos.notification.NewCommentNotificationService;

/**
 *
 */
public class ScriptCommentService extends BaseScopableProcessorExtension {

    private NewCommentNotificationService newCommentNotificationService;

    public String onAddComment(String nodeRef, String comment, String commentLink,
                               String author, String subscribersString) {
        newCommentNotificationService.notify(
                nodeRef,
                comment,
                commentLink,
                author,
		        subscribersString
        );
        return "";
    }

    public NewCommentNotificationService getNewCommentNotificationService() {
        return newCommentNotificationService;
    }

    public void setNewCommentNotificationService(NewCommentNotificationService newCommentNotificationService) {
        this.newCommentNotificationService = newCommentNotificationService;
    }
}

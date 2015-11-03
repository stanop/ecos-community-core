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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import ru.citeck.ecos.security.NodeOwnerDAO;
import ru.citeck.ecos.utils.ReflectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public class NewCommentNotificationService {

    private NotificationService notificationService;
    private NodeService nodeService;
    private AuthorityService authorityService;
	private NodeOwnerDAO nodeOwnerDAO;

    public static final NodeRef NEW_COMMENT_NOTIFICATION_EMAIL_TEMPLATE = new NodeRef(
            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
            "comment-notify-email-html-ftl"
    );

	public static interface TemplateArgAlias {
        final String COMMENT_TEXT = "commentText";
        final String COMMENT_LINK = "nodeDetailsLink";
        final String COMMENT_AUTHOR = "commentAuthor";
        final String FILE_NAME = "fileName";
    };

    /**
     * Уведомление о добавлении нового комментария
     * @param nodeRef
     * @param comment
     * @param commentLink
     */
    public void notify(
            final String nodeRef,
            final String comment,
            final String commentLink,
            final String author,
            final String subscribersString) {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                notificationService.sendNotification(
                        EMailNotificationProvider.NAME,
                        getNewCommentNotification(nodeRef, comment, commentLink, author, subscribersString)
                );
                return null;
            }
        });
    }

    private NotificationContext getNewCommentNotification(String nodeRefId, String comment,
                                                          String commentLink, String author, String subscribersString) {
        NodeRef nodeRef = new NodeRef(nodeRefId);
        if (null == nodeRef) {
            throw new IllegalArgumentException();
        }
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.setSubject("Уведомление");
        
        // notificationContext.setBodyTemplate(NEW_COMMENT_NOTIFICATION_EMAIL_TEMPLATE);
        // NOTE: for compatibility with Alfresco Community 4.2.c
        NodeRef template = NEW_COMMENT_NOTIFICATION_EMAIL_TEMPLATE;
        ReflectionUtils.callSetterIfDeclared(notificationContext, "setBodyTemplate", template);
        ReflectionUtils.callSetterIfDeclared(notificationContext, "setBodyTemplate", template.toString());

        notificationContext.setTemplateArgs(
                getNewCommentNotificationTemplateArgs(
                        nodeRef,
                        comment,
                        commentLink,
                        author
                )
        );
        Set<String> assignedAuthorities = getNotificationListeners(nodeRef);
	    assignedAuthorities.addAll(new HashSet<String>(Arrays.asList(subscribersString.split(","))));
        for (String authority : assignedAuthorities) {
            notificationContext.addTo(authority);
        }
        notificationContext.setAsyncNotification(true);
        return notificationContext;
    }

    private Map<String, Serializable> getNewCommentNotificationTemplateArgs(NodeRef nodeRef, String comment, String commentLink, String author) {
        Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
        templateArgs.put(TemplateArgAlias.COMMENT_TEXT, comment);
        templateArgs.put(TemplateArgAlias.COMMENT_LINK, commentLink);
        templateArgs.put(TemplateArgAlias.FILE_NAME, getFileName(nodeRef));
        templateArgs.put(TemplateArgAlias.COMMENT_AUTHOR, getCommentAuthor(author));
        return templateArgs;
    }

    private Set<String> getNotificationListeners(NodeRef nodeRef) {
        Set<String> authorities = new HashSet<String>();
        authorities.addAll(getNodeOwners(nodeRef));
        return authorities;
    }

    private List<String> getNodeOwners(NodeRef nodeRef) {
        String authority = nodeOwnerDAO.getOwner(nodeRef);
        return (null != authority)?
                Arrays.asList(new String[]{ authority }) :
                Collections.<String>emptyList();
    }

    private String getFileName(NodeRef nodeRef) {
        String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        return (null != fileName)? fileName : "неизвестно";
    }

    private String getCommentAuthor(String userId) {
        NodeRef nodeRef = authorityService.getAuthorityNodeRef(userId);
        String firstName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME);
        return (null != firstName && null != lastName)?
                String.format("%s %s", firstName, lastName) :
                (null != firstName || null != lastName)? ((null != firstName)? firstName : lastName) : userId;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

	public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
		this.nodeOwnerDAO = nodeOwnerDAO;
	}

}

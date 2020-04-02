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
package ru.citeck.ecos.behavior.common;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.behavior.OrderedBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.IdocsTemplateModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AutoNameBehaviour implements
    NodeServicePolicies.OnUpdatePropertiesPolicy,
    NodeServicePolicies.OnMoveNodePolicy,
    VersionServicePolicies.OnCreateVersionPolicy {

    private static Log logger = LogFactory.getLog(AutoNameBehaviour.class);

    private static ConcurrentHashMap<Object, Boolean> nameCache;

    static {
        nameCache = new ConcurrentHashMap<>(100);
    }

    // constants
    private static final String EMPTY_EXTENSION = "";

    // common properties
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private MimetypeService mimetypeService;
    private TemplateService templateService;
    private ScriptService scriptService;

    // distinct properties
    private QName className;
    private String nodeVariable;
    private String nameTemplate;
    private String nameTemplateJS;
    private boolean generateFromTitle = false;
    private Map<String,String> restrictedPatterns;
    private boolean ignoreRenameFailure = false;
    private Boolean appendExtension = null;
    private int order = 100;
    private boolean enabled;

    public void init() {
        if (nameTemplate != null && nameTemplateJS != null) {
            throw new AlfrescoRuntimeException("Only one of 'nameTemplate' and 'nameTemplateJS' props must be set, not both" );
        }
        OrderedBehaviour updateBehaviour = new OrderedBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, className, updateBehaviour);
        OrderedBehaviour moveBehaviour = new OrderedBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT, order);
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnMoveNodePolicy.QNAME, className, moveBehaviour);
        OrderedBehaviour createBehaviour = new OrderedBehaviour(this, "onCreateVersion", NotificationFrequency.TRANSACTION_COMMIT, order);
        policyComponent.bindClassBehaviour(VersionServicePolicies.OnCreateVersionPolicy.QNAME, className, createBehaviour);
        if (appendExtension == null) {
            appendExtension = dictionaryService.isSubClass(className, ContentModel.TYPE_CONTENT);
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {
        if (enabled) {
            updateName(nodeRef, false);
        }
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef,
                           ChildAssociationRef newChildAssocRef) {
        if (enabled) {
            updateName(newChildAssocRef.getChildRef(), true);
        }
    }

    @Override
    public void onCreateVersion(QName classRef,
                                NodeRef nodeRef,
                                Map<String, Serializable> versionProperties,
                                PolicyScope nodeDetails) {
        if (enabled) {
            updateName(nodeRef, false);
        }
    }

    private void updateName(final NodeRef nodeRef, boolean forceRename) {
        try {
            if (!nodeService.exists(nodeRef)) {
                return;
            }
            // do not auto-name working copies
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
                return;
            }

            if (!generateFromTitle && StringUtils.isEmpty(nameTemplate) && StringUtils.isEmpty(nameTemplateJS)) {
                return;
            }

            String oldName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            int pos = oldName.lastIndexOf('.');
            if (!appendExtension && pos >= 0) {
                nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, oldName.substring(0, pos));
            }
            String oldExtension = pos >= 0 ? oldName.substring(pos) : EMPTY_EXTENSION;

            String newName = AuthenticationUtil.runAsSystem(() -> {

                if (generateFromTitle) {

                    Locale localeBefore = I18NUtil.getLocale();
                    I18NUtil.setLocale(Locale.getDefault());

                    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

                    String title = (String) props.get(ContentModel.PROP_TITLE);
                    if (StringUtils.isEmpty(title)) {
                        title = (String) props.get(ContentModel.PROP_NAME);
                    }

                    I18NUtil.setLocale(localeBefore);

                    return title;
                }

                HashMap<String,Object> model = new HashMap<>(1);

                model.put(nodeVariable, nodeRef);

                if (nameTemplateJS != null) {

                    model.put("msg", (Function<String, String>) key -> {
                        String message = I18NUtil.getMessage(key, Locale.getDefault());
                        return message != null ? message : key;
                    });

                    return scriptService.executeScriptString(nameTemplateJS, model).toString();
                } else {
                    return templateService.processTemplateString("freemarker", nameTemplate, model);
                }
            });

            // automatically replace all restricted patterns in name:
            boolean matches;
            do {
                matches = false;
                for (Map.Entry<String, String> entry : restrictedPatterns.entrySet()) {
                    String restrictedPattern = entry.getKey();
                    if (newName.matches(".*" + restrictedPattern + ".*")) {
                        newName = newName.replaceAll(restrictedPattern, entry.getValue());
                        matches = true;
                    }
                }
            } while (matches);

            // if empty name returned
            // do not perform rename
            if (newName.isEmpty()) {
                return;
            }

            // get extension
            String extension = EMPTY_EXTENSION;
            if (appendExtension) {
                extension = RepoUtils.getExtension(nodeRef, EMPTY_EXTENSION, nodeService, mimetypeService);
            }
            String oldGeneratedName = (String) nodeService.getProperty(nodeRef, IdocsTemplateModel.PROP_GENERATED_NAME);
            boolean equal = newName.equals(oldGeneratedName) && extension.equals(oldExtension);

            // change name if new name differs from old name
            if (!equal || forceRename) {

                final String finalBaseName = newName;
                final String finalExtension = extension;

                AuthenticationUtil.runAsSystem(() -> {

                    nodeService.setProperty(nodeRef, IdocsTemplateModel.PROP_GENERATED_NAME, finalBaseName);
                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
                    NodeRef parentRef = parentAssoc.getParentRef();
                    int index = 0;

                    // find unique name:
                    String newName1 = finalBaseName + finalExtension;
                    while (true) {
                        NodeRef existingNode = nodeService.getChildByName(parentRef,
                                                                          parentAssoc.getTypeQName(),
                                                                          newName1);
                        if(nodeRef.equals(existingNode)) {
                            break;
                        }
                        if(existingNode == null) {

                            // check name cache
                            final Object key = new Pair<>(parentRef, newName1);
                            boolean isFree = nameCache.putIfAbsent(key, true) == null;

                            if (isFree) {
                                AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                                    @Override
                                    public void afterCommit() {
                                        nameCache.remove(key);
                                    }
                                    @Override
                                    public void afterRollback() {
                                        nameCache.remove(key);
                                    }
                                });

                                try {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(Thread.currentThread().getName() +
                                                     ": trying to set name: " + newName1);
                                    }

                                    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName1);

                                    if (logger.isDebugEnabled()) {
                                        logger.debug(Thread.currentThread().getName() + ": success");
                                    }
                                    break;
                                } catch (DuplicateChildNodeNameException e) {
                                    logger.warn(Thread.currentThread().getName() +
                                                ": failure to set name " + newName1);
                                    // proceed
                                }
                            }
                        }
                        index++;
                        newName1 = finalBaseName + " (" + index + ")" + finalExtension;
                    }
                    return null;
                });
            }
        } catch (RuntimeException e) {
            if (!ignoreRenameFailure) {
                throw e;
            }
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public void setClassName(QName className) {
        this.className = className;
    }

    public void setNameTemplate(String nameTemplate) {
        this.nameTemplate = nameTemplate;
    }

    public void setNameTemplateJS(String nameTemplateJS) {
        this.nameTemplateJS = nameTemplateJS;
    }

    public void setNodeVariable(String nodeVariable) {
        this.nodeVariable = nodeVariable;
    }

    public void setRestrictedPatterns(Map<String,String> restrictedPatterns) {
        this.restrictedPatterns = restrictedPatterns;
    }

    public void setGenerateFromTitle(boolean generateFromTitle) {
        this.generateFromTitle = generateFromTitle;
    }

    public void setIgnoreRenameFailure(boolean ignore) {
        this.ignoreRenameFailure = ignore;
    }

    public void setAppendExtension(Boolean appendExtension) {
        this.appendExtension = appendExtension;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled.booleanValue();
    }

}

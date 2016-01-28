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
package ru.citeck.ecos.form;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.*;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.EncodingFieldProcessor;
import org.alfresco.repo.forms.processor.node.MimetypeFieldProcessor;
import org.alfresco.repo.forms.processor.node.SizeFieldProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DATA_KEY_SEPARATOR;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP;

/**
 * Duplicates given NodeRef during persist() calling
 * NOTE: if you want use this processor, you must add "create-form" configs inside share form
 * configuration, FOR node-type evaluator!!!
 * @author deathNC
 */
public class DuplicatorFormProcessor extends ContentModelFormProcessor<NodeRef, NodeRef> {

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(DuplicatorFormProcessor.class);

    private static QName ASPECT_FILE_PLAN_COMPONENT = QName.createQName("http://www.alfresco.org/model/recordsmanagement/1.0", "filePlanComponent");

    protected static final String NAME_PROP_DATA = PROP + DATA_KEY_SEPARATOR + "cm" + DATA_KEY_SEPARATOR + "name";

    /**
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger()
     */
    @Override
    protected Log getLogger() {
        return logger;
    }

    /**
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(org.alfresco.repo.forms.Item)
     */
    @Override
    protected NodeRef getTypedItem(Item item) {
        // create NodeRef representation, the id could already be in a valid
        // NodeRef format or it may be in a URL friendly format
        NodeRef nodeRef = null;
        if (NodeRef.isNodeRef(item.getId())) {
            nodeRef = new NodeRef(item.getId());
        } else {
            // split the string into the 3 required parts
            String[] parts = item.getId().split("/");
            if (parts.length == 3) {
                try {
                    nodeRef = new NodeRef(parts[0], parts[1], parts[2]);
                } catch (IllegalArgumentException iae) {
                    // ignored for now, dealt with below

                    if (logger.isDebugEnabled())
                        logger.debug("NodeRef creation failed for: " + item.getId(), iae);
                }
            }
        }

        // check we have a valid node ref
        if (nodeRef == null) {
            throw new FormNotFoundException(item, new IllegalArgumentException(item.getId()));
        }

        // check the node itself exists
        if (this.nodeService.exists(nodeRef) == false) {
            throw new FormNotFoundException(item,
                    new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef));
        } else {
            // all Node based filters can expect to get a NodeRef
            return nodeRef;
        }
    }


    /**
     * (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
     */
    @Override
    protected String getItemType(NodeRef item) {
        QName type = this.nodeService.getType(item);
        return type.toPrefixString(this.namespaceService);
    }

    /**
     * (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(NodeRef item) {
        StringBuilder builder = new StringBuilder("/api/node/");
        builder.append(item.getStoreRef().getProtocol()).append("/");
        builder.append(item.getStoreRef().getIdentifier()).append("/");
        builder.append(item.getId());
        return builder.toString();
    }

    @Override
    protected Map<QName, Serializable> getPropertyValues(NodeRef nodeRef) {
        return nodeService.getProperties(nodeRef);
    }

    @Override
    protected Map<QName, Serializable> getAssociationValues(NodeRef item) {
        HashMap<QName, Serializable> assocs = new HashMap<QName, Serializable>();
        List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(item, RegexQNamePattern.MATCH_ALL);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(item);
        for (ChildAssociationRef childAssoc : childAssocs) {
            QName name = childAssoc.getTypeQName();
            NodeRef target = childAssoc.getChildRef();
            addAssocToMap(name, target, assocs);
        }
        for (AssociationRef associationRef : targetAssocs) {
            QName name = associationRef.getTypeQName();
            NodeRef target = associationRef.getTargetRef();
            addAssocToMap(name, target, assocs);
        }
        return assocs;
    }

    @SuppressWarnings("unchecked")
    private void addAssocToMap(QName name, NodeRef target, HashMap<QName, Serializable> assocs) {
        Serializable value = assocs.get(name);
        if (value == null) {
            LinkedHashSet<NodeRef> values = new LinkedHashSet<NodeRef>();
            values.add(target);
            assocs.put(name, values);
        } else {
            if (value instanceof Set<?>) {
                ((Set<NodeRef>) value).add(target);
            }
        }
    }

    @Override
    protected Map<String, Object> getTransientValues(NodeRef item) {
        Map<String, Object> values = new HashMap<String, Object>(3);
        ContentData contentData = getContentData(item);
        if (contentData != null) {
            values.put(EncodingFieldProcessor.KEY, contentData.getEncoding());
            values.put(MimetypeFieldProcessor.KEY, contentData.getMimetype());
            values.put(SizeFieldProcessor.KEY, contentData.getSize());
        }
        return values;
    }

    @Override
    protected Set<QName> getAspectNames(NodeRef nodeRef) {
        return nodeService.getAspects(nodeRef);
    }

    @Override
    protected TypeDefinition getBaseType(NodeRef nodeRef) {
        QName typeName = nodeService.getType(nodeRef);
        return dictionaryService.getType(typeName);
    }

    private ContentData getContentData(NodeRef nodeRef) {
        // Checks if the node is content and if so gets the ContentData
        QName type = this.nodeService.getType(nodeRef);
        ContentData content = null;
        if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
            content = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }
        return content;
    }

    /**
     * Determines whether the given node represents a working copy, if it does
     * the name field is searched for and set to protected as the name field
     * should not be edited for a working copy.
     * <br>
     * If the node is not a working copy this method has no effect.
     *
     * @param nodeRef NodeRef of node to check and potentially process
     * @param form    The generated form
     */
    protected void processWorkingCopy(NodeRef nodeRef, Form form) {
        // if the node is a working copy ensure that the name field (id present)
        // is set to be protected as it can not be edited
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
            // go through fields looking for name field
            for (FieldDefinition fieldDef : form.getFieldDefinitions()) {
                if (fieldDef.getName().equals(ContentModel.PROP_NAME.toPrefixString(this.namespaceService))) {
                    fieldDef.setProtectedField(true);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Set " + ContentModel.PROP_NAME.toPrefixString(this.namespaceService) +
                                "field to protected as it is a working copy");
                    }
                    break;
                }
            }
        }
    }

    /**
     * (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(NodeRef item, List<String> fields, List<String> forcedFields, Form form,
                                    Map<String, Object> context) {
        super.internalGenerate(item, fields, forcedFields, form, context);
        processWorkingCopy(item, form);
    }

    @Override
    protected NodeRef internalPersist(NodeRef item, final FormData data) {
        if (logger.isDebugEnabled())
            logger.debug("Persisting form for: " + item);
        // create a new instance of the type
        final NodeRef nodeRef = createNode(item, data);

        if (nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT)) {
            // persist the form data as the admin user
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                    public Object doWork() throws Exception {
                        persistNode(nodeRef, data);
                        return null;
                    }
                },
                AuthenticationUtil.getSystemUserName()
            );
        } else {
            // persist the form data
            persistNode(nodeRef, data);
        }
        // return the newly created node
        return nodeRef;
    }

    /**
     * <p>Create a new clean node, which duplicates type of sourceNode.</p>
     * <p>
     * The new node is placed in the location defined by the "destination" data
     * item in the form data (this will usually be a hidden field) or, if destination
     * is not defined, node will be placed into parent of sourceNode. This will
     * also be the NodeRef representation of the parent for the new node.
     * </p>
     * @param sourceNode is node, which will be duplicated
     * @param data The form data
     * @return NodeRef representing the newly created node
     * */
    protected NodeRef createNode(NodeRef sourceNode, FormData data) {
        NodeRef nodeRef = null;

        if (data != null) {
            // getting type of sourceNode
            QName typeDefName = this.nodeService.getType(sourceNode);

            // getting node by data.destinarion or getting parent node for sourceNode
            NodeRef parentRef = null;
            FormData.FieldData destination = data.getFieldData(DESTINATION);
            if (destination == null) {
                parentRef = this.nodeService.getPrimaryParent(sourceNode).getParentRef();
            } else {
                parentRef = new NodeRef((String) destination.getValue());
            }
            // if parentNode not exists - throw exception
            if (parentRef == null) {
                throw new FormException("Failed to persist form for '"
                        + typeDefName.toPrefixString(this.namespaceService) +
                        "'! 'formdata.destination' or 'sourceNode.parent' is not defined!");
            }
            // remove the destination data to avoid warning during persistence,
            // this can always be retrieved by looking up the created node's parent
            data.removeFieldData(DESTINATION);

            // if a name property is present in the form data remove it from this data
            FormData.FieldData nameData = data.getFieldData(NAME_PROP_DATA);
            if (nameData != null) {
                data.removeFieldData(NAME_PROP_DATA);
            }
            // next we generate a guid
            String nodeName = GUID.generate();

            // create the node
            Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>(1);
            nodeProps.put(ContentModel.PROP_NAME, nodeName);
            nodeRef = this.nodeService.createNode(
                    parentRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName)),
                    typeDefName, nodeProps
            ).getChildRef();
        }

        return nodeRef;
    }

}

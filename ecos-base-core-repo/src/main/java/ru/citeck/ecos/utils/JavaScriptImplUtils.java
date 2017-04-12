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
package ru.citeck.ecos.utils;

import java.io.Serializable;
import java.util.*;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.repo.security.authority.script.ScriptUser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptImplUtils {

    private static final Log LOGGER = LogFactory.getLog(JavaScriptImplUtils.class);

    public static ScriptNode wrapNode(String nodeRef,
            AlfrescoScopableProcessorExtension scopeProvider) {
        return wrapNode(new NodeRef(nodeRef), scopeProvider);
    }

	public static ScriptNode wrapNode(NodeRef nodeRef,
			AlfrescoScopableProcessorExtension scopeProvider)
	{
	    if(nodeRef == null) return null;
		return new ScriptNode(nodeRef, scopeProvider.getServiceRegistry(), scopeProvider.getScope());
	}
	
	public static ScriptNode[] wrapNodes(Collection<NodeRef> nodeRefs, 
			AlfrescoScopableProcessorExtension scopeProvider) 
	{
		List<ScriptNode> nodes = new ArrayList<ScriptNode>(nodeRefs.size());
		for(NodeRef nodeRef : nodeRefs) {
			ScriptNode node = wrapNode(nodeRef, scopeProvider);
			if(node != null) {
				nodes.add(node);
			}
		}
		return nodes.toArray(new ScriptNode[nodes.size()]);
	}
	
	public static ScriptGroup wrapGroup(String groupName, 
			AlfrescoScopableProcessorExtension scopeProvider) 
	{
		if(groupName == null) {
			return null;
		}
		return new ScriptGroup(groupName, scopeProvider.getServiceRegistry(), scopeProvider.getScope());
	}
	
	public static ScriptGroup[] wrapGroups(Collection<String> groupNames, 
			AlfrescoScopableProcessorExtension scopeProvider) 
	{
		ScriptGroup[] groups = new ScriptGroup[groupNames.size()];
		int i = 0;
		for(String groupName : groupNames) {
			groups[i++] = wrapGroup(groupName, scopeProvider);
		}
		return groups;
	}
	
	public static ScriptUser wrapUser(String userName, 
			AlfrescoScopableProcessorExtension scopeProvider) 
	{
		return new ScriptUser(userName, null, scopeProvider.getServiceRegistry(), scopeProvider.getScope());
	}
	
	public static ScriptUser[] wrapUsers(Collection<String> userNames, 
			AlfrescoScopableProcessorExtension scopeProvider) 
	{
		ScriptUser[] users = new ScriptUser[userNames.size()];
		int i = 0;
		for(String userName : userNames) {
			users[i++] = wrapUser(userName, scopeProvider);
		}
		return users;
	}
	
    public static ScriptNode wrapAuthorityAsNode(String authorityName, 
            AlfrescoScopableProcessorExtension scopeProvider) {
        NodeRef nodeRef = null;
        if(authorityName.startsWith(AuthorityType.GROUP.getPrefixString())) {
            AuthorityService authorityService = scopeProvider.getServiceRegistry().getAuthorityService();
            nodeRef = authorityService.getAuthorityNodeRef(authorityName);
        } else {
            PersonService personService = scopeProvider.getServiceRegistry().getPersonService();
            nodeRef = personService.getPerson(authorityName);
        }
        if(nodeRef == null) return null;
        return wrapNode(nodeRef, scopeProvider);
    }
    
    public static ScriptNode[] wrapAuthoritiesAsNodes(Collection<String> authorityNames, 
            AlfrescoScopableProcessorExtension scopeProvider) {
        List<ScriptNode> nodes = new ArrayList<ScriptNode>(authorityNames.size());
        for(String authorityName : authorityNames) {
            ScriptNode node = wrapAuthorityAsNode(authorityName, scopeProvider);
            if(node != null) {
                nodes.add(node);
            }
        }
        return nodes.toArray(new ScriptNode[nodes.size()]);
    }
    
    public static void extractPropertiesMap(ScriptableObject scriptable, Map<QName, Serializable> map, ValueConverter converter, NamespaceService namespaceService) {
        for (Object propertyName : scriptable.getIds()) {
            if (propertyName instanceof String) {
                String name = (String) propertyName;
                Object propertyValue = scriptable.get(name, scriptable);
                if (propertyValue instanceof Serializable) {
                    Serializable value = converter.convertValueForRepo((Serializable) propertyValue);
                    map.put(convertQName(name, namespaceService), value);
                }
            }
        }
    }

    public static Map<String, Object> convertScriptableToMap(ScriptableObject scriptable) {
        Map<String, Object> values = new HashMap<String, Object>();
        for (Object propertyName : scriptable.getIds()) {
            if (propertyName instanceof String) {
                String name = (String) propertyName;
                Object propertyValue = scriptable.get(name, scriptable);
                values.put(name, propertyValue);
            }
        }
        return values;
    }
    
    public static Map<String, Serializable> convertScriptableToSerializableMap(ScriptableObject scriptable) {
        Map<String, Serializable> values = new HashMap<String, Serializable>();
        ValueConverter converter = new ValueConverter();
        for (Object propertyName : scriptable.getIds()) {
            if (propertyName instanceof String) {
                String name = (String) propertyName;
                Object propertyValue = scriptable.get(name, scriptable);
                if (propertyValue instanceof Serializable) {
                    Serializable value1 = converter.convertValueForRepo((Serializable) propertyValue);
                    values.put(name, value1);
                }
            }
        }
        return values;
    }
    
    public static QName convertQName(String string, NamespaceService namespaceService) {
        QName qname = null;
        if (string.indexOf(QName.NAMESPACE_BEGIN) != -1) {
            qname = QName.createQName(string);
        } else if (namespaceService != null) {
            qname = QName.createQName(string, namespaceService);
        }
        return qname;
    }
    
    public static String convertQName(QName qname, NamespaceService namespaceService) {
        return qname.toPrefixString(namespaceService);
    }
    
    public static String[] convertQNames(Collection<QName> qnames, NamespaceService namespaceService) {
        String[] strings = new String[qnames.size()];
        int i = 0;
        for(QName qname : qnames) {
            strings[i++] = convertQName(qname, namespaceService);
        }
        return strings;
    }
    
    public static Collection<QName> convertQNames(String[] strings, NamespaceService namespaceService) {
        Collection<QName> qnames = new ArrayList<>(strings.length);
        for(String string : strings) {
            qnames.add(convertQName(string, namespaceService));
        }
        return qnames;
    }

    public static NodeRef getNodeRef(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof NodeRef) {
            return (NodeRef) object;
        }
        if (object instanceof String) {
            return new NodeRef((String) object);
        }
        if (object instanceof ScriptNode) {
            return ((ScriptNode) object).getNodeRef();
        }
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to NodeRef");
    }

    public static NodeRef getAuthorityRef(Object authority, AuthorityService authorityService) {
        if (authority instanceof String) {
            String strAuthority = (String) authority;
            if (NodeRef.isNodeRef(strAuthority)) {
                return new NodeRef(strAuthority);
            }
            NodeRef authorityRef = authorityService.getAuthorityNodeRef(strAuthority);
            if (authorityRef != null) {
                return authorityRef;
            } else {
                throw new IllegalArgumentException("Authority with name '" + strAuthority + "' not found!");
            }
        }
        return getNodeRef(authority);
    }

    public static List<NodeRef> getAuthoritiesList(Object object, AuthorityService authorityService) {
        List<NodeRef> authorities = new ArrayList<>();
        fillAuthorities(authorities, object, authorityService);
        return authorities;
    }

    public static Set<NodeRef> getAuthoritiesSet(Object object, AuthorityService authorityService) {
        Set<NodeRef> authorities = new HashSet<>();
        fillAuthorities(authorities, object, authorityService);
        return authorities;
    }

    private static void fillAuthorities(Collection<NodeRef> collection, Object value, AuthorityService authorityService) {
        if (value == null) return;

        if (value instanceof NodeRef) {

            collection.add((NodeRef) value);

        } else if (value instanceof ScriptNode) {

            collection.add(((ScriptNode) value).getNodeRef());

        } else if (value instanceof String) {

            String strValue = (String) value;
            if (NodeRef.isNodeRef(strValue)) {
                collection.add(new NodeRef(strValue));
            } else {
                NodeRef nodeRef = authorityService.getAuthorityNodeRef(strValue);
                if (nodeRef != null) {
                    collection.add(nodeRef);
                } else {
                    LOGGER.warn("Authority with name '" + value + "' not found!");
                }
            }

        } else if (value instanceof Collection) {

            for (Object item : (Collection) value) {
                fillAuthorities(collection, item, authorityService);
            }
        }
    }
}

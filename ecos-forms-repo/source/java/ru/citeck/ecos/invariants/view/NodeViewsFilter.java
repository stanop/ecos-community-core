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
package ru.citeck.ecos.invariants.view;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.invariants.view.NodeViewElement.Key;

import java.util.*;

class NodeViewsFilter {

    private static final Log logger = LogFactory.getLog(NodeViewsFilter.class);

    private NamespacePrefixResolver prefixResolver;
    private DictionaryService dictionaryService;

    private SearchNode rootNode;

    private Map<String, Collection<NodeViewElement>> elementsBySource;
    
    /*
     * Initialization methods.
     */

    public NodeViewsFilter() {
        rootNode = new SearchNode();
        elementsBySource = new HashMap<>();
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    /*
     * Registration methods.
     */

    public Collection<NodeViewElement> getRegisteredViews(String sourceId) {
        Collection<NodeViewElement> registeredViews = elementsBySource.get(sourceId);
        return registeredViews != null
                ? Collections.unmodifiableCollection(registeredViews)
                : null;
    }

    public void registerViews(Collection<NodeViewElement> elements, String sourceId) {
        unregisterViews(sourceId);
        elements = new ArrayList<>(elements);
        rootNode.registerElements(elements);
        elementsBySource.put(sourceId, elements);
    }

    public void unregisterViews(Collection<NodeViewElement> elements) {
        if (elements == null) return;
        rootNode.unregisterElements(elements);
    }

    public void unregisterViews(String sourceId) {
        unregisterViews(elementsBySource.remove(sourceId));
    }
    
    /*
     * Resolution methods.
     */

    public boolean isViewRegistered(NodeView view) {
        // view is registered if there is any matching concrete element
        return SearchNode.search(
                Collections.singletonList(rootNode), view,
                new SearchNode.Predicate() {
                    public boolean check(NodeViewElement element, SearchNode node) {
                        return element.isConcrete();
                    }
                }) != null;
    }

    public NodeView resolveView(NodeView view) {
        return (NodeView) resolveElement(view, Collections.singletonList(rootNode));
    }

    private NodeViewElement resolveElement(NodeViewElement element, List<SearchNode> searchRoots) {
        NodeViewElement.Builder<?> builder = NodeViewElement.Builder
                .getBuilder(element, prefixResolver)
                .dictionary(dictionaryService);

        final Set<SearchNode> usedNodes = new LinkedHashSet<>();
        final Set<NodeViewElement> mergedElements = new HashSet<>();
        Set<Key> childKeys = new HashSet<>();
        Set<NodeViewElement> children = new LinkedHashSet<>();

        for (NodeViewElement child : element.getElements()) {
            if (!child.isConcrete()) continue;
            Key key = child.getKey();
            if (key == null || childKeys.add(key)) {
                children.add(child);
            }
        }

        // fill with default properties (such as datatype for fields)
        element = builder.merge(element).build();

        if (searchRoots != null)
            while (true) {

                NodeViewElement matchingElement = SearchNode.search(searchRoots, element,
                        new SearchNode.Predicate() {
                            public boolean check(NodeViewElement matchingElement, SearchNode node) {
                                if (mergedElements.contains(matchingElement)) return false;
                                usedNodes.add(node);
                                return true;
                            }
                        });

                if (logger.isDebugEnabled()) {
                    logger.debug("Searching for: " + element + ", found " + matchingElement);
                }

                if (matchingElement == null) break;

                builder.merge(matchingElement);
                mergedElements.add(matchingElement);

                // process elements
                List<NodeViewElement> elements = matchingElement.getElements();
                for (NodeViewElement child : elements) {
                    if (!child.isConcrete()) continue;
                    Key key = child.getKey();
                    if (key == null || childKeys.add(key)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Added concrete child: " + child);
                        }
                        children.add(child);
                    }
                }

                // update view:
                element = builder.build();
            }

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + children.size() + " concrete children for " + element + ", processing...");
        }

        // resolve child elements
        List<SearchNode> childSearchRoots = new ArrayList<>(usedNodes);
        childSearchRoots.add(rootNode);
        List<NodeViewElement> resolvedChildren = new ArrayList<>(children.size());
        for (NodeViewElement child : children) {
            resolvedChildren.add(this.resolveElement(child, childSearchRoots));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Children processing finished for " + element);
        }

        return builder
                .elements(resolvedChildren)
                .build();
    }

    private static class SearchNode {
        private Set<NodeViewElement> roots;
        private Map<Key, SearchNode> elements;
        
        /*
         * Registration methods.
         */

        public void registerElements(Collection<? extends NodeViewElement> elements) {
            if (this.elements == null) this.elements = new HashMap<>();
            for (NodeViewElement element : elements) {
                if (element == null) continue;
                List<Key> keys = element.getIndexKeys();
                if (keys.isEmpty()) continue;
                for (Key key : keys) {
                    registerElement(element, key, this.elements);
                }
            }
        }

        private void registerElement(NodeViewElement element, Key key, Map<Key, SearchNode> elementsMap) {
            SearchNode node = getOrCreateAndPut(elementsMap, key, searchNodeProducer);
            node.registerRoot(element);
        }

        private void registerRoot(NodeViewElement element) {
            if (roots == null) {
                roots = new HashSet<>();
            }
            roots.add(element);
            registerElements(element.getElements());
        }
        
        /*
         * Un-registration methods.
         */

        public void unregisterElements(Collection<? extends NodeViewElement> elements) {
            if (this.elements == null) return;
            for (NodeViewElement element : elements) {
                if (element == null) continue;
                List<Key> keys = element.getIndexKeys();
                if (keys.isEmpty()) continue;
                for (Key key : keys) {
                    unregisterElement(element, key, this.elements);
                }
            }
        }

        private void unregisterElement(NodeViewElement element, Key key,
                                       Map<Key, SearchNode> elementsMap) {
            if (elementsMap == null)
                return;
            SearchNode node = elementsMap.get(key);
            if (node == null)
                return;
            node.unregisterRoot(element);
        }

        private void unregisterRoot(NodeViewElement element) {
            if (roots == null) return;
            roots.remove(element);
            unregisterElements(element.getElements());
        }

        /*
         * Searching methods.
         */

        public static NodeViewElement search(List<SearchNode> searchRoots, NodeViewElement element, Predicate predicate) {
            if (searchRoots == null) return null;
            List<SearchNode> nodes = new LinkedList<>();
            for (SearchNode searchRoot : searchRoots) {
                NodeViewElement value = searchRoot.search(element, predicate, nodes);
                if (value != null) return value;
            }
            return null;
        }

        private NodeViewElement search(NodeViewElement element, Predicate predicate, List<SearchNode> nodes) {
            if (elements == null) return null;
            for (Key key : element.getSearchKeys()) {
                SearchNode node = elements.get(key);
                if (node == null) continue;
                for (NodeViewElement root : node.roots) {
                    if (predicate.check(root, node)) return root;
                }
            }
            return null;
        }

        private static <K, V> V getOrCreateAndPut(Map<K, V> map, K key, Producer<V> producer) {
            V value = map.get(key);
            if (value == null) {
                value = producer.create();
                map.put(key, value);
            }
            return value;
        }

        private interface Producer<V> {
            V create();
        }

        private static Producer<SearchNode> searchNodeProducer = new Producer<SearchNode>() {
            public SearchNode create() {
                return new SearchNode();
            }
        };

        private interface Predicate {

            boolean check(NodeViewElement source, SearchNode node);

        }
    }

}

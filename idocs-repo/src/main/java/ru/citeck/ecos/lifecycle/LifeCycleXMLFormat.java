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
package ru.citeck.ecos.lifecycle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleAction;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleCondition;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleEvent;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleState;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;
import ru.citeck.ecos.model.LifeCycleModel;

/**
 * Base parser of XML LifeCycle Definition
 *
 * @author Alexey Moiseev <alexey.moiseev@citeck.ru>
 */
public class LifeCycleXMLFormat extends LifeCycleAbstractFormat {

    public static final String NAME = "xml";

    public static final String SCHEMA_PATH = "alfresco/module/idocs-repo/schema/lifecycle.xsd";

    public static final String NS_URL = "http://www.citeck.ru/lifecycle/1.0";
    public static final String NS_PREFIX = "lc:";

    public static Log logger = LogFactory.getLog(LifeCycleXMLFormat.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public LifeCycleDefinition parseLifeCycleDefinition(InputStream lifeCycleDefinitionStream) throws IOException {

    	LifeCycleDefinition result = new LifeCycleDefinition();

        Document xmlDoc = getDOMDocument(lifeCycleDefinitionStream, "UTF-8");

        if (xmlDoc != null) {
        	try {
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
				nsContext.addNamespace("lc", NS_URL);
				xpath.setNamespaceContext(nsContext);

				XPathExpression expr = xpath.compile("/" + NS_PREFIX + LifeCycleConstants.LIFECYCLE + "/" + NS_PREFIX + LifeCycleConstants.STATE);
				NodeList nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

				logger.info("Found " + nodes.getLength() + " states in XML document with lifecycle definition");

				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					LifeCycleState state = getStateData(result, node, xpath);

					if (state != null)
						result.getStateList().add(state);
				}

				expr = xpath.compile("/" + NS_PREFIX + LifeCycleConstants.LIFECYCLE + "/" + NS_PREFIX + LifeCycleConstants.TRANSITION);
				nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

				logger.info("Found " + nodes.getLength() + " transitions in XML document with lifecycle definition");

				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					LifeCycleTransition transition = getTransitionData(result, node, xpath);

					if (transition != null)
						result.getTransitionList().add(transition);
				}
        	} catch (XPathException e) {
        		logger.error(e.getMessage(), e);
        		logger.error("Unable to parse lifecycle definition");
        	}
        }

        return result;
    }

    protected Document getDOMDocument(InputStream xmlStream, String encoding) {
		DocumentBuilderFactory DOMFactory = DocumentBuilderFactory.newInstance();
        DOMFactory.setNamespaceAware(true);

        InputStreamReader reader = null;

        try {
        	// validate document
        	logger.info("Validating XML LifeCycle Definition against XSD schema");

        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
       		IOUtils.copy(xmlStream, baos);
       		InputStream xmlStream1 = new ByteArrayInputStream(baos.toByteArray());
       		InputStream xmlStream2 = new ByteArrayInputStream(baos.toByteArray());

       		final ClassPathResource xsdResource = new ClassPathResource(SCHEMA_PATH);
       		InputStream xsdStream = xsdResource.getInputStream();

       		validateDocument(xmlStream1, xsdStream);

       		// parse document
        	logger.info("Building W3C Document for lifecycle XML stream");

            reader = new InputStreamReader(xmlStream2, encoding);
            InputSource is = new InputSource(reader);

            // build document
        	DocumentBuilder builder = DOMFactory.newDocumentBuilder();
        	Document doc = builder.parse(is);

        	return doc;
        } catch (SAXException | IOException | ParserConfigurationException e) {
			logger.error(e.getMessage(), e);

			return null;
		} finally {
            if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
            }
        }
	}

    /**
     * Validate XML document against XSD schema
     *
     * @param xmlStream Stream with XML document
     * @param xsdStream Stream with XSD document
     * @return <b>true</b> if document is valid and <b>false</b> if document is invalid or unable to perform validation
     * @throws SAXException
     * @throws IOException
     */
    protected void validateDocument(InputStream xmlStream, InputStream xsdStream) throws SAXException, IOException {
    	SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlStream));
    }

    /**
     * Extracts state data from XML node
     *
     * @param node XML node
     * @return POJO with state fields
     */
    protected LifeCycleState getStateData(LifeCycleDefinition lcd, Node node, XPath xpath) {
    	if (node != null) {
    		LifeCycleState state = new LifeCycleState();

    		try {
				Node idNode = (Node) xpath.evaluate(NS_PREFIX + LifeCycleConstants.ID, node, XPathConstants.NODE);
				Node eventNode = (Node) xpath.evaluate(NS_PREFIX + LifeCycleConstants.EVENT, node, XPathConstants.NODE);
				NodeList startActionNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.START_ACTION, node, XPathConstants.NODESET);
				NodeList endActionNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.END_ACTION, node, XPathConstants.NODESET);

				if (idNode != null) {
					String id = getNodeStringValue(idNode);
					state.setId(id);
				}

				if (eventNode != null) {
					LifeCycleEvent lcEvent = getLifeCycleEvent(lcd, xpath, eventNode);
					state.setEvent(lcEvent);
				}

				if ((startActionNodes != null) && (startActionNodes.getLength() > 0)) {
				    for (int i = 0; i < startActionNodes.getLength(); i++) {
    					Node actionNode = startActionNodes.item(i);
    					Node typeNode = actionNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_TYPE);

    					if (typeNode != null) {
    						String type = typeNode.getNodeValue();
    						LifeCycleAction lcAction = new LifeCycleAction(type);

    						NodeList paramNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.PARAM, actionNode, XPathConstants.NODESET);

    		                for (int j = 0; j < paramNodes.getLength(); j++) {
    		                    Node paramNode = paramNodes.item(j);
    		                    Node nameNode = paramNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_NAME);

    		                    if (nameNode != null) {
    		                        String paramName = nameNode.getNodeValue();
    		                        String paramValue = getNodeStringValue(paramNode);
    		                        lcAction.setParam(paramName, paramValue);
    		                    }
    		                }

    		                state.getStartActionList().add(lcAction);
    					}
				    }
				}

				if ((endActionNodes != null) && (endActionNodes.getLength() > 0)) {
				    for (int i = 0; i < endActionNodes.getLength(); i++) {
    					Node actionNode = endActionNodes.item(i);
    					Node typeNode = actionNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_TYPE);

    					if (typeNode != null) {
    						String type = typeNode.getNodeValue();
    						LifeCycleAction lcAction = new LifeCycleAction(type);

                            NodeList paramNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.PARAM, actionNode, XPathConstants.NODESET);

                            for (int j = 0; j < paramNodes.getLength(); j++) {
                                Node paramNode = paramNodes.item(j);
                                Node nameNode = paramNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_NAME);

                                if (nameNode != null) {
                                    String paramName = nameNode.getNodeValue();
                                    String paramValue = getNodeStringValue(paramNode);
                                    lcAction.setParam(paramName, paramValue);
                                }
                            }

                            state.getEndActionList().add(lcAction);
    					}
				    }
				}

				return state;
			} catch (XPathException e) {
				logger.error(e.getMessage(), e);
			}
    	}

    	return null;
    }

    /**
     * Extracts transition data from XML node
     *
     * @param node XML node
     * @return POJO with transition fields
     */
    protected LifeCycleTransition getTransitionData(LifeCycleDefinition lcd, Node node, XPath xpath) {
    	if (node != null) {
    		LifeCycleTransition transition = new LifeCycleTransition();

    		try {
				Node fromStateNode = (Node) xpath.evaluate(NS_PREFIX + LifeCycleConstants.FROM_STATE, node, XPathConstants.NODE);
				Node eventNode = (Node) xpath.evaluate(NS_PREFIX + LifeCycleConstants.EVENT, node, XPathConstants.NODE);
				Node toStateNode = (Node) xpath.evaluate(NS_PREFIX + LifeCycleConstants.TO_STATE, node, XPathConstants.NODE);
				NodeList conditionNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.CONDITION, node, XPathConstants.NODESET);
				NodeList actionNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.ACTION, node, XPathConstants.NODESET);

				if (fromStateNode != null) {
					String fromState = getNodeStringValue(fromStateNode);
					transition.setFromState(fromState);
				}

				if (toStateNode != null) {
					String toState = getNodeStringValue(toStateNode);
					transition.setToState(toState);
				}

				if (eventNode != null) {
					LifeCycleEvent lcEvent = getLifeCycleEvent(lcd, xpath, eventNode);
					transition.setEvent(lcEvent);
				}

				if ((conditionNodes != null) && (conditionNodes.getLength() > 0)) {
				    for (int i = 0; i < conditionNodes.getLength(); i++) {
    					Node conditionNode = conditionNodes.item(i);
    					Node typeNode = conditionNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_TYPE);

    					if (typeNode != null) {
    						String type = typeNode.getNodeValue();
    						LifeCycleCondition lcCondition = new LifeCycleCondition(type);

                            NodeList paramNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.PARAM, conditionNode, XPathConstants.NODESET);

                            for (int j = 0; j < paramNodes.getLength(); j++) {
                                Node paramNode = paramNodes.item(j);
                                Node nameNode = paramNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_NAME);

                                if (nameNode != null) {
                                    String paramName = nameNode.getNodeValue();
                                    String paramValue = getNodeStringValue(paramNode);
                                    lcCondition.setParam(paramName, paramValue);
                                }
                            }

                            transition.getConditionList().add(lcCondition);
    					}
				    }
				}

				if ((actionNodes != null) && (actionNodes.getLength() > 0)) {
				    for (int i = 0; i < actionNodes.getLength(); i++) {
    					Node actionNode = actionNodes.item(i);
    					Node typeNode = actionNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_TYPE);

    					if (typeNode != null) {
    						String type = typeNode.getNodeValue();
    						LifeCycleAction lcAction = new LifeCycleAction(type);

                            NodeList paramNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.PARAM, actionNode, XPathConstants.NODESET);

                            for (int j = 0; j < paramNodes.getLength(); j++) {
                                Node paramNode = paramNodes.item(j);
                                Node nameNode = paramNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_NAME);

                                if (nameNode != null) {
                                    String paramName = nameNode.getNodeValue();
                                    String paramValue = getNodeStringValue(paramNode);
                                    lcAction.setParam(paramName, paramValue);
                                }
                            }

                            transition.getActionList().add(lcAction);
    					}
				    }
				}

				return transition;
			} catch (XPathException e) {
				logger.error(e.getMessage(), e);
			}
    	}

    	return null;
    }

    protected LifeCycleEvent getLifeCycleEvent(LifeCycleDefinition lcd, XPath xpath, Node eventNode) throws XPathException {
    	Node typeNode = eventNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_TYPE);

		if (typeNode != null) {
			LifeCycleEvent lcEvent = new LifeCycleEvent();

			String type = typeNode.getNodeValue();

			String eventType = "";
			switch (type) {
				case "auto" :
					eventType = LifeCycleModel.CONSTR_AUTOMATIC_TRANSITION;
					break;
				case "user" :
					eventType = LifeCycleModel.CONSTR_USER_TRANSITION;
					break;
				case "timer" :
					eventType = LifeCycleModel.CONSTR_TIMER_TRANSITION;
					break;
				case "processStart" :
					eventType = LifeCycleModel.CONSTR_TRANSITION_ON_START_PROCESS;
					break;
				case "processEnd" :
					eventType = LifeCycleModel.CONSTR_TRANSITION_ON_END_PROCESS;
					break;
				case "signal" :
                    eventType = LifeCycleModel.CONSTR_TRANSITION_ON_SIGNAL;
                    break;
			}

			lcEvent.setEventType(eventType);

			NodeList paramNodes = (NodeList) xpath.evaluate(NS_PREFIX + LifeCycleConstants.PARAM, eventNode, XPathConstants.NODESET);

			if (paramNodes != null) {
				for (int i = 0; i < paramNodes.getLength(); i++) {
					Node paramNode = paramNodes.item(i);
					Node nameNode = paramNode.getAttributes().getNamedItem(LifeCycleConstants.ATTR_NAME);

					if (nameNode != null) {
						String paramName = nameNode.getNodeValue();
						String paramValue = getNodeStringValue(paramNode);
						lcEvent.setEventParam(paramName, paramValue);
					}
				}
			}

			return lcEvent;
		}

		return null;
    }

    protected String getNodeStringValue(Node n) {
		try {
			return n.getTextContent();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

    @Override
    public String serializeLifeCycleDefinition(LifeCycleDefinition lcd) {
        if (lcd != null) {
            try {
                Document document = createNewDocument();

                Element rootElement = document.createElement(LifeCycleConstants.LIFECYCLE);
                document.appendChild(rootElement);
                rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                rootElement.setAttribute("xmlns", NS_URL);
                rootElement.setAttribute("xsi:schemaLocation", NS_URL + " lifecycle.xsd");

                serializeStateList(document, rootElement, lcd.getStateList());
                serializeTransitionList(document, rootElement, lcd.getTransitionList());

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                DOMSource source = new DOMSource(document);
                Writer outWriter = new StringWriter();
                StreamResult result = new StreamResult(outWriter);
                transformer.transform(source, result);

                return outWriter.toString();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return null;
    }

    protected Document createNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        return documentBuilder.newDocument();
    }

    protected void serializeStateList(Document document, Element rootEl, List<LifeCycleState> stateList) {
        if (stateList != null) {
            for (LifeCycleState lcState : stateList) {
                Element stateEl = document.createElement(LifeCycleConstants.STATE);
                rootEl.appendChild(stateEl);

                // id
                Element idEl = document.createElement(LifeCycleConstants.ID);
                idEl.setTextContent(lcState.getId());
                stateEl.appendChild(idEl);

                // event
                LifeCycleEvent lcEvent = lcState.getEvent();
                if (lcEvent != null) {
                    Element eventEl = document.createElement(LifeCycleConstants.EVENT);
                    eventEl.setAttribute(LifeCycleConstants.ATTR_TYPE, getXMLEventType(lcEvent.getEventType()));

                    List<Element> eventParamElements = serializeParameters(document, lcEvent.getEventParams());
                    for (Element el : eventParamElements)
                        eventEl.appendChild(el);

                    stateEl.appendChild(eventEl);
                }

                // start actions
                List<LifeCycleAction> lcStartActions = lcState.getStartActionList();
                if (lcStartActions != null) {
                    for (LifeCycleAction lcAction : lcStartActions) {
                        Element actionEl = document.createElement(LifeCycleConstants.START_ACTION);
                        actionEl.setAttribute(LifeCycleConstants.ATTR_TYPE, lcAction.getType());

                        List<Element> actionParamElements = serializeParameters(document, lcAction.getParams());
                        for (Element el : actionParamElements)
                            actionEl.appendChild(el);

                        stateEl.appendChild(actionEl);
                    }
                }

                // end actions
                List<LifeCycleAction> lcEndActions = lcState.getEndActionList();
                if (lcEndActions != null) {
                    for (LifeCycleAction lcAction : lcEndActions) {
                        Element actionEl = document.createElement(LifeCycleConstants.END_ACTION);
                        actionEl.setAttribute(LifeCycleConstants.ATTR_TYPE, lcAction.getType());

                        List<Element> actionParamElements = serializeParameters(document, lcAction.getParams());
                        for (Element el : actionParamElements)
                            actionEl.appendChild(el);

                        stateEl.appendChild(actionEl);
                    }
                }
            }
        }
    }

    protected void serializeTransitionList(Document document, Element rootEl, List<LifeCycleTransition> transitionList) {
        if (transitionList != null) {
            for (LifeCycleTransition lcTransition : transitionList) {
                Element transitionEl = document.createElement(LifeCycleConstants.TRANSITION);
                rootEl.appendChild(transitionEl);

                // from state
                Element fromStateEl = document.createElement(LifeCycleConstants.FROM_STATE);
                fromStateEl.setTextContent(lcTransition.getFromState());
                transitionEl.appendChild(fromStateEl);

                // event
                LifeCycleEvent lcEvent = lcTransition.getEvent();
                Element eventEl = document.createElement(LifeCycleConstants.EVENT);
                eventEl.setAttribute(LifeCycleConstants.ATTR_TYPE, getXMLEventType(lcEvent.getEventType()));

                List<Element> eventParamElements = serializeParameters(document, lcEvent.getEventParams());
                for (Element el : eventParamElements)
                    eventEl.appendChild(el);

                transitionEl.appendChild(eventEl);

                // to state
                Element toStateEl = document.createElement(LifeCycleConstants.TO_STATE);
                toStateEl.setTextContent(lcTransition.getToState());
                transitionEl.appendChild(toStateEl);

                // conditions
                List<LifeCycleCondition> lcConditions = lcTransition.getConditionList();
                if (lcConditions != null) {
                    for (LifeCycleCondition lcCondition : lcConditions) {
                        Element conditionEl = document.createElement(LifeCycleConstants.CONDITION);
                        conditionEl.setAttribute(LifeCycleConstants.ATTR_TYPE, lcCondition.getType());

                        List<Element> conditionParamElements = serializeParameters(document, lcCondition.getParams());
                        for (Element el : conditionParamElements)
                            conditionEl.appendChild(el);

                        transitionEl.appendChild(conditionEl);
                    }
                }

                // actions
                List<LifeCycleAction> lcActions = lcTransition.getActionList();
                if (lcActions != null) {
                    for (LifeCycleAction lcAction : lcActions) {
                        Element actionEl = document.createElement(LifeCycleConstants.ACTION);
                        actionEl.setAttribute(LifeCycleConstants.ATTR_TYPE, lcAction.getType());

                        List<Element> actionParamElements = serializeParameters(document, lcAction.getParams());
                        for (Element el : actionParamElements)
                            actionEl.appendChild(el);

                        transitionEl.appendChild(actionEl);
                    }
                }
            }
        }
    }

    protected List<Element> serializeParameters(Document document, Map<String, String> parameters) {
        List<Element> elList = new ArrayList<Element>();

        if (parameters != null) {
            for (String key : parameters.keySet()) {
                if (parameters.get(key) != null) {
                    Element paramEl = document.createElement(LifeCycleConstants.PARAM);
                    paramEl.setAttribute(LifeCycleConstants.ATTR_NAME, key);

                    CDATASection cdata = document.createCDATASection(parameters.get(key));
                    paramEl.appendChild(cdata);

                    elList.add(paramEl);
                }
            }
        }

        return elList;
    }

    protected String getXMLEventType(String eventType) {
        String xmlEventType = "";

        switch (eventType) {
            case LifeCycleModel.CONSTR_AUTOMATIC_TRANSITION :
                xmlEventType = "auto";
                break;
            case LifeCycleModel.CONSTR_USER_TRANSITION :
                xmlEventType = "user";
                break;
            case LifeCycleModel.CONSTR_TIMER_TRANSITION :
                xmlEventType = "timer";
                break;
            case LifeCycleModel.CONSTR_TRANSITION_ON_START_PROCESS :
                xmlEventType = "processStart";
                break;
            case LifeCycleModel.CONSTR_TRANSITION_ON_END_PROCESS :
                xmlEventType = "processEnd";
                break;
            case LifeCycleModel.CONSTR_TRANSITION_ON_SIGNAL :
                xmlEventType = "signal";
                break;
        }

        return xmlEventType;
    }

	class SimpleNamespaceContext implements NamespaceContext {
		private Map<String, String> urisByPrefix = new HashMap<String, String>();

		private Map<String, Set<String>> prefixesByURI = new HashMap<String, Set<String>>();

		public SimpleNamespaceContext() {
			addNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
			addNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		}

		public synchronized void addNamespace(String prefix, String namespaceURI) {
			urisByPrefix.put(prefix, namespaceURI);
			if (prefixesByURI.containsKey(namespaceURI)) {
				(prefixesByURI.get(namespaceURI)).add(prefix);
			} else {
				Set<String> set = new HashSet<String>();
				set.add(prefix);
				prefixesByURI.put(namespaceURI, set);
			}
		}

		public String getNamespaceURI(String prefix) {
			if (prefix == null)
				throw new IllegalArgumentException("Prefix cannot be null");
			if (urisByPrefix.containsKey(prefix))
				return (String) urisByPrefix.get(prefix);
			else
				return XMLConstants.NULL_NS_URI;
		}

		public String getPrefix(String namespaceURI) {
			return (String) getPrefixes(namespaceURI).next();
		}

		public Iterator<String> getPrefixes(String namespaceURI) {
			if (namespaceURI == null)
				throw new IllegalArgumentException("NamespaceURI cannot be null");
			if (prefixesByURI.containsKey(namespaceURI)) {
				return ((Set<String>) prefixesByURI.get(namespaceURI)).iterator();
			} else {
				return (new HashSet<String>()).iterator();
			}
		}
	}
}

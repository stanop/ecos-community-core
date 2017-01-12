package ru.citeck.ecos.dictionary;

import org.alfresco.repo.web.scripts.dictionary.ClassesGet;
import org.alfresco.repo.web.scripts.dictionary.DictionaryComparators;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.*;

    public class EcosDictionaryClassesGet extends ClassesGet {

        public EcosDictionaryClassesGet() {}

        private static Log logger = LogFactory.getLog(EcosDictionaryClassesGet.class);

        private static final String MODEL_PROP_KEY_CLASS_DEFS = "classdefs";
        private static final String MODEL_PROP_KEY_PROP_DETAILS = "propertydefs";
        private static final String MODEL_PROP_KEY_ASSOC_DETAILS = "assocdefs";
        private static final String CF_TYPE_ALL = "all";
        private static final String CF_TYPE_ASPECT = "aspect";
        private static final String CF_TYPE_TYPE = "type";
        private static final String REQ_PARAM_CLASS_FILTER = "cf";
        private static final String REQ_PARAM_CHILDREN_TYPE_BY_PARENT = "ctbp";
        private static final String REQ_PARAM_NAME = "n";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String classFilter = getValidInput(req.getParameter(REQ_PARAM_CLASS_FILTER));
        String childrenTypeByParent = getValidInput(req.getParameter(REQ_PARAM_CHILDREN_TYPE_BY_PARENT));
        String name = getValidInput(req.getParameter(REQ_PARAM_NAME));
        Map<QName, ClassDefinition> classdef = new HashMap<>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<>();
        Map<String, Object> model = new HashMap<>();
        Collection<QName> qnames = new ArrayList<>();
        QName myModel = null;

        if(classFilter == null) {
            classFilter = CF_TYPE_ALL;
        }

        if(!isValidClassFilter(classFilter)) {
            throw new WebScriptException(404, "Check the classfilter - " + classFilter + " provided in the URL");
        } else if(childrenTypeByParent == null && name != null) {
            throw new WebScriptException(404, "Missing childrenTypeByParent parameter in the URL - both combination of name and childrenTypeByParent is needed");
        } else {
            if(childrenTypeByParent != null) {
                String prefix = childrenTypeByParent.split(":")[0];
                String localName = childrenTypeByParent.split(":")[1];
                myModel = getQNameForModel(prefix, localName);
                qnames = dictionaryservice.getSubTypes(myModel, true);

                if(name != null) {
                    for (QName qname: qnames) {
                        if (name.equals(qname.getLocalName())) {
                            QName classQname = getClassQname(qname.getPrefixString().split(":")[0], name);
                            classdef.put(classQname, dictionaryservice.getClass(classQname));
                            propdef.put(classQname, dictionaryservice.getClass(classQname).getProperties().values());
                            assocdef.put(classQname, dictionaryservice.getClass(classQname).getAssociations().values());
                        }
                    }
                } else {
                    if(classFilter.equalsIgnoreCase(CF_TYPE_ALL)) {
                        qnames.addAll(dictionaryservice.getSubAspects(myModel, true));
                        qnames.addAll(dictionaryservice.getSubTypes(myModel, true));
                    } else if(classFilter.equalsIgnoreCase(CF_TYPE_TYPE)) {
                        qnames.addAll(dictionaryservice.getSubTypes(myModel, true));
                    } else if(classFilter.equalsIgnoreCase(CF_TYPE_ASPECT)) {
                        qnames.addAll(dictionaryservice.getSubAspects(myModel, true));
                    }
                }
            }

            if(myModel == null) {
                if(classFilter.equalsIgnoreCase(CF_TYPE_ALL)) {
                    qnames.addAll(dictionaryservice.getAllAspects());
                    qnames.addAll(dictionaryservice.getAllTypes());
                } else if(classFilter.equalsIgnoreCase(CF_TYPE_TYPE)) {
                    qnames.addAll(dictionaryservice.getAllTypes());
                } else if(classFilter.equalsIgnoreCase(CF_TYPE_ASPECT)) {
                    qnames.addAll(dictionaryservice.getAllAspects());
                }
            }

            if(classdef.isEmpty()) {
                for (QName qname: qnames) {
                    classdef.put(qname, dictionaryservice.getClass(qname));
                    propdef.put(qname, dictionaryservice.getClass(qname).getProperties().values());
                    assocdef.put(qname, dictionaryservice.getClass(qname).getAssociations().values());
                }
            }

            List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(classdef.values());
            Collections.sort(classDefinitions, new DictionaryComparators.ClassDefinitionComparator(dictionaryservice));
            model.put(MODEL_PROP_KEY_CLASS_DEFS, classDefinitions);
            model.put(MODEL_PROP_KEY_PROP_DETAILS, reorderedValues(classDefinitions, propdef));
            model.put(MODEL_PROP_KEY_ASSOC_DETAILS, reorderedValues(classDefinitions, assocdef));
            model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, dictionaryservice);
            return model;
        }
    }
}

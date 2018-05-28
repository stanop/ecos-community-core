package ru.citeck.ecos.webscripts.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.forms.EcosFormService;
import ru.citeck.ecos.forms.FormMode;
import ru.citeck.ecos.forms.NodeViewDefinition;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeView;
import ru.citeck.ecos.invariants.view.NodeViewElement;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.search.SearchCriteria;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;
import ru.citeck.ecos.webscripts.utils.WebScriptUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EcosFormNodeViewGet extends AbstractWebScript {

    /*========PARAMS========*/
    private static final String PARAM_FORM_TYPE = "formType";
    private static final String PARAM_FORM_MODE = "formMode";
    private static final String PARAM_FORM_KEY = "formKey";
    private static final String PARAM_FORM_ID = "formId";
    /*=======/PARAMS========*/
    private static final String TEMPLATE_PARAM_PREFIX = "param_";

    @Autowired
    private EcosFormService ecosFormService;
    @Autowired
    private EcosNsPrefixResolver prefixResolver;
    @Autowired
    private DictionaryService dictionaryService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        res.setContentEncoding("utf-8");

        String formType = req.getParameter(PARAM_FORM_TYPE);
        String formModeStr = req.getParameter(PARAM_FORM_MODE);
        String formKey = req.getParameter(PARAM_FORM_KEY);
        String formId = req.getParameter(PARAM_FORM_ID);

        FormMode mode = null;
        if (formModeStr != null) {
            mode = Enum.valueOf(FormMode.class, formModeStr.toUpperCase());
        }
        NodeViewDefinition view = ecosFormService.getNodeView(formType, formKey, formId, mode, getTemplateParams(req));

        Response response = new Response();
        response.canBeDraft = view.canBeDraft;
        response.view = view.nodeView != null ? new View(view.nodeView) : null;
        response.formType = formType;
        response.formKey = formKey;

        if (NodeRef.isNodeRef(formKey)) {
            response.nodeRef = formKey;
        }
        if (response.view == null) {
            res.setStatus(Status.STATUS_NOT_FOUND);
        }

        objectMapper.writeValue(res.getWriter(), response);
    }

    private Map<String, Object> getTemplateParams(WebScriptRequest req) {
        Map<String, String> requestParams = WebScriptUtils.getParameterMap(req);
        Map<String, Object> templateParams = new HashMap<>(requestParams.size());
        for (String key : requestParams.keySet()) {
            if (key.startsWith(TEMPLATE_PARAM_PREFIX)) {
                templateParams.put(key.replaceFirst(TEMPLATE_PARAM_PREFIX, ""), requestParams.get(key));
            }
        }
        return templateParams;
    }

    private String formatQName(QName qname) {
        if (qname == null) {
            return null;
        }
        return qname.toPrefixString(prefixResolver);
    }

    private String formatToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private class Response {
        public View view;
        public boolean canBeDraft = false;
        public String formType;
        public String formKey;
        public String nodeRef;
    }

    private class Element {

        public String type;
        public String id;
        public String kind;
        public String template;
        public Map<String, Object> params;

        public Element(String type, NodeViewElement element) {
            this.type = type;
            this.id = element.getId();
            this.kind = element.getKind();
            this.template = element.getTemplate();
            this.params = element.getParams();
        }
    }

    private class Region extends Element {

        public String name;

        public Region(NodeViewRegion region) {
            super("region", region);
            name = region.getName();
        }
    }

    private class Scope {

        @JsonProperty(value = "class")
        public String clazz;
        public String classKind;
        public String attribute;
        public String attributeKind;

        Scope(InvariantScope scope) {
            clazz = formatQName(scope.getClassScope());
            classKind = formatClassScopeKind(scope.getClassScopeKind());
            attribute = formatQName(scope.getAttributeScope());
            attributeKind = formatToString(scope.getAttributeScopeKind());
        }

        private String formatClassScopeKind(InvariantScope.ClassScopeKind kind) {
            if (kind == null) {
                return null;
            }
            return kind.name();
        }
    }

    private class Invariant {

        public Scope scope;
        public String feature;
        public String priority;
        public String description;
        @JsonProperty(value = "final")
        public boolean isFinal;
        public String language;
        public Object expression;

        Invariant(InvariantDefinition definition) {

            InvariantScope invScope = definition.getScope();
            scope = invScope != null ? new Scope(invScope) : null;

            feature = formatToString(definition.getFeature());
            priority = formatToString(definition.getPriority());
            description = definition.getDescription();
            isFinal = definition.isFinal();
            language = definition.getLanguage();

            Object value = definition.getValue();
            if (value instanceof SearchCriteria) {
                expression = ((SearchCriteria) value).getTriplets();
            } else {
                expression = value;
            }
        }
    }

    private class Field extends Element {

        public String attribute;
        public String datatype;
        public String nodetype;
        public List<Region> regions;
        public List<Invariant> invariants;
        public String fieldType;
        public String javaclass;

        public Field(NodeField field) {
            super("field", field);
            attribute = formatQName(field.getAttributeName());
            datatype = formatQName(field.getDatatypeName());
            nodetype = formatQName(field.getNodetypeName());
            regions = field.getRegions().stream().map(Region::new).collect(Collectors.toList());
            invariants = field.getInvariants().stream().map(Invariant::new).collect(Collectors.toList());
            if (field.isAssociation()) {
                AssociationDefinition assoc = dictionaryService.getAssociation(field.getAttributeName());
                fieldType = assoc != null && assoc.isChild() ? "child-association" : "association";
            } else {
                fieldType = "property";
            }
            javaclass = field.getJavaclass();
        }
    }

    private class View extends Element {

        @JsonProperty(value = "class")
        public String clazz;
        public String mode;

        public List<Element> elements = new ArrayList<>();

        public View(NodeView nodeView) {
            super("view", nodeView);

            clazz = formatQName(nodeView.getClassName());
            mode = formatToString(nodeView.getMode());

            for (NodeViewElement nvElement : nodeView.getElements()) {
                if (nvElement instanceof NodeView) {
                    elements.add(new View((NodeView) nvElement));
                } else if (nvElement instanceof NodeField) {
                    elements.add(new Field((NodeField) nvElement));
                }
            }
        }
    }
}

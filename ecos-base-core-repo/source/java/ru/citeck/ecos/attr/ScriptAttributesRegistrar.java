package ru.citeck.ecos.attr;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;
import ru.citeck.ecos.attr.prov.VirtualScriptAttributes;

/**
 * @author Pavel Simonov
 */
public class ScriptAttributesRegistrar {

    private NamespaceService namespaceService;
    private VirtualScriptAttributes scriptAttributes;

    private String name;
    private String className;

    private String title;
    private MLText mlTitle;

    private String script;

    public void init() {
        checkFields();
        if (mlTitle == null) {
            mlTitle = new MLText();
            mlTitle.addValue(I18NUtil.getLocale(), title);
        }
        QName classQName = QName.resolveToQName(namespaceService, className);
        QName attributeQName = QName.resolveToQName(namespaceService, name);
        scriptAttributes.registerAttribute(classQName, attributeQName, mlTitle, script);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMlTitle(MLText mlTitle) {
        this.mlTitle = mlTitle;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public void setVirtualScriptAttributes(VirtualScriptAttributes virtualScriptAttributes) {
        this.scriptAttributes = virtualScriptAttributes;
    }

    private void checkFields() {
        if ((mlTitle == null || mlTitle.isEmpty()) && (title == null || title.isEmpty()) ) {
            throw new AlfrescoRuntimeException("One of mlTitle or title must be defined");
        }
        ParameterCheck.mandatoryString("className", className);
        ParameterCheck.mandatoryString("name", name);
        ParameterCheck.mandatoryString("script", script);
    }

}

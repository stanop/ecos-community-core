package ru.citeck.ecos.graphql.node;

import lombok.Getter;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.graphql.GqlContext;

import java.util.Collection;

/**
 * @author Pavel Simonov
 */
public class GqlQName {

    @Getter(lazy = true)
    private final String prefix = evalPrefix();
    @Getter(lazy = true)
    private final ClassDefinition classDefinition = evalClassDef();

    private QName qname;

    private GqlContext context;

    public GqlQName(QName name, GqlContext context) {
        this.qname = name;
        this.context = context;
    }

    public GqlQName(String name, GqlContext context) {
        this.qname = QName.resolveToQName(context.getNamespaceService(), name);
    }

    public QName getQName() {
        return qname;
    }

    public String prefix() {
        return getPrefix();
    }

    public String classTitle() {
        return getClassDefinition().getTitle(context.getMessageService());
    }

    public String namespace() {
        return qname.getNamespaceURI();
    }

    public String localName() {
        return qname.getLocalName();
    }

    public String fullName() {
        return String.format("{%s}%s", qname.getNamespaceURI(), qname.getLocalName());
    }

    public String shortName() {
        return String.format("%s:%s", getPrefix(), qname.getLocalName());
    }

    private String evalPrefix() {
        NamespaceService ns = context.getNamespaceService();
        Collection<String> prefixes = ns.getPrefixes(qname.getNamespaceURI());
        return prefixes.stream().findFirst().orElse("");
    }

    private ClassDefinition evalClassDef() {
        return context.getDictionaryService().getClass(qname);
    }
}

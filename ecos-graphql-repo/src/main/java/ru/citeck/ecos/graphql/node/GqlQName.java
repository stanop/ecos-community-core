package ru.citeck.ecos.graphql.node;

import graphql.annotations.annotationTypes.GraphQLField;
import lombok.Getter;
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

    private QName qname;

    private GqlContext context;

    public GqlQName(QName name, GqlContext context) {
        this.qname = name;
        this.context = context;
    }

    public GqlQName(String name, GqlContext context) {
        this.qname = QName.resolveToQName(context.getNamespaceService(), name);
    }

    @GraphQLField
    public String prefix() {
        return getPrefix();
    }

    @GraphQLField
    public String namespace() {
        return qname.getNamespaceURI();
    }

    @GraphQLField
    public String localName() {
        return qname.getLocalName();
    }

    @GraphQLField
    public String fullName() {
        return String.format("{%s}%s", qname.getNamespaceURI(), qname.getLocalName());
    }

    @GraphQLField
    public String shortName() {
        return String.format("%s:%s", getPrefix(), qname.getLocalName());
    }

    private String evalPrefix() {
        NamespaceService ns = context.getNamespaceService();
        Collection<String> prefixes = ns.getPrefixes(qname.getNamespaceURI());
        return prefixes.stream().findFirst().orElse("");
    }
}

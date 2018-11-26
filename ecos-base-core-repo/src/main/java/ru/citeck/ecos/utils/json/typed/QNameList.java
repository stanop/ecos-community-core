package ru.citeck.ecos.utils.json.typed;

import org.alfresco.service.namespace.QName;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pavel Simonov
 */
public class QNameList extends ArrayList<QName> {

    public QNameList(int initialCapacity) {
        super(initialCapacity);
    }

    public QNameList() {
    }

    public QNameList(Collection<? extends QName> c) {
        super(c);
    }
}

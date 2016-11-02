package ru.citeck.ecos.invariants;

import ru.citeck.ecos.invariants.InvariantsRuntime.RuntimeNode;
import org.alfresco.service.namespace.QName;

import java.util.Map;
import java.util.Objects;

public class InvariantsUnstableException extends InvariantsRuntimeException {

    public InvariantsUnstableException(RuntimeNode node, int executionsCount) {
        super(String.format("Invariants executed %s times and don't reach stable state. " +
                            "Please check invariants for this attributes: %s",
                            executionsCount, getUnstableAttributes(node)));
    }

    private static String getUnstableAttributes(RuntimeNode node) {
        node.reset();
        Map<QName, Object> attributes0 = node.getNewAttributeValues();
        node.reset();
        Map<QName, Object> attributes1 = node.getNewAttributeValues();

        StringBuilder sb = new StringBuilder();
        for (QName key : attributes0.keySet()) {
            if (!Objects.equals(attributes0.get(key), attributes1.get(key))) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(key.toPrefixString());
            }
        }
        return sb.toString();
    }
}

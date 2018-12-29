package ru.citeck.ecos.records.attributes;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attributes.accessor.AccessorProvider;
import ru.citeck.ecos.records.attributes.accessor.ObjectAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecordAttributesDAO {

    private Map<String, AccessorProvider> accessors = new ConcurrentHashMap<>();

    //TODO: add support of internal attributes
    public ObjectAccessor getObjectAttsAccessor(Map<String, String> attributes) {

        StringBuilder sb = new StringBuilder("o(");

        attributes.forEach((pseudoName, path) -> {

            String fieldName = path.replace("/", "");

            String accessor = null;
            int questionIdx = path.indexOf('?');

            boolean isFlatField = fieldName.equals("str") || fieldName.equals("id");

            if (questionIdx >= 0) {
                fieldName = path.substring(0, questionIdx);
                accessor = path.substring(questionIdx + 1);
            } else {
                if (!isFlatField) {
                    accessor = "s";
                }
            }

            boolean isMultiple = false;
            int arrayIdx = fieldName.indexOf("[]");
            if (arrayIdx >= 0) {
                fieldName = fieldName.substring(0, arrayIdx);
                isMultiple = true;
            }

            sb.append('\'')
                    .append(pseudoName)
                    .append("':");

            if (isFlatField) {
                sb.append("o(").append(fieldName);
            } else {
                sb.append("a(")
                        .append(fieldName)
                        .append(',')
                        .append(isMultiple)
                        .append(")?o(");
            }

            if (accessor != null) {
                if (isFlatField){
                    sb.append("?");
                }
                sb.append(accessor);
            }
            sb.append("),");
        });
        sb.setLength(sb.length() - 1);
        sb.append(")");

        return (ObjectAccessor) getAccessor(sb.toString());
    }

    public AttributeAccessor getAccessor(String accessorStr) {

        int idx = accessorStr.length();
        int brackets = 0;

        List<String> parameters = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        boolean paramsFilling = false;

        AttributeAccessor currentAccessor = null;

        while (--idx >= 0) {

            char ch = accessorStr.charAt(idx);

            if (ch == '\'') {
                sb.append(ch);
                while ((ch = accessorStr.charAt(--idx)) != '\'') {
                    sb.append(ch);
                }
                sb.append(ch);
                continue;
            }

            if (!paramsFilling && ch == '?' || idx == 0) {

                if (idx == 0) {
                    sb.append(ch);
                }

                AccessorProvider provider = accessors.get(toTrimmedStr(sb.reverse()));
                currentAccessor = provider.getAccessor(parameters, currentAccessor);
                sb.setLength(0);

            } else if (ch == ')') {

                paramsFilling = true;
                if (brackets != 0) {
                    sb.append(')');
                }
                brackets++;

            } else if (ch == '(') {

                if (--brackets == 0) {
                    fillParameter(sb, parameters, paramsFilling, idx, accessorStr);
                    paramsFilling = false;
                } else {
                    sb.append('(');
                }

            } else if (brackets == 1 && ch == ',') {

                fillParameter(sb, parameters, paramsFilling, idx, accessorStr);

            } else {

                sb.append(ch);
            }
        }

        return currentAccessor;
    }

    private void fillParameter(StringBuilder sb,
                               List<String> parameters,
                               boolean fillParameters,
                               int charIdx,
                               String accessorStr) {

        String param = fillParameters ? toTrimmedStr(sb.reverse()) : "";
        if (param.isEmpty()) {
            throwUnexpectedChar(',', charIdx, accessorStr);
        }
        parameters.add(0, param);
        sb.setLength(0);
    }

    private String toTrimmedStr(StringBuilder sb) {
        if (sb.length() == 0) {
            return "";
        }
        int from = 0;
        int to = sb.length() - 1;
        while (from < sb.length() && isTrimmedChar(sb.charAt(from))) {
            from++;
        }
        if (from == sb.length()) {
            return "";
        }
        while (to >= 0 && isTrimmedChar(sb.charAt(to))) {
            to--;
        }
        return sb.substring(from, to + 1);
    }

    private boolean isTrimmedChar(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
    }

    private void throwUnexpectedChar(char ch, int at, String accessorStr) {
        throw new IllegalArgumentException("Unexpected character '" + ch + "' at " + at +
                                           ": Accessor string: " + accessorStr);
    }

    public void register(AccessorProvider accessor) {
        accessors.put(accessor.getId(), accessor);
    }
}

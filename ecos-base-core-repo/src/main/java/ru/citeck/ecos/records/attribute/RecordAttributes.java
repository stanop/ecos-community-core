package ru.citeck.ecos.records.attribute;

import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.attribute.accessor.AccessorProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecordAttributes {

    private Map<String, AccessorProvider> accessors = new ConcurrentHashMap<>();

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

                AccessorProvider provider = accessors.get(sb.reverse().toString());
                currentAccessor = provider.getAccessor(parameters, currentAccessor);
                sb.setLength(0);

            } else if (ch == ')') {

                paramsFilling = true;
                brackets++;

            } else if (ch == '(') {

                if (--brackets == 0) {
                    fillParameter(sb, parameters, paramsFilling, idx, accessorStr);
                    paramsFilling = false;
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

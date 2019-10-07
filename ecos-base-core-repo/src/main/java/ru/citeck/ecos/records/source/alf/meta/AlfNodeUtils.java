package ru.citeck.ecos.records.source.alf.meta;

public class AlfNodeUtils {

    public static String resolveHasContentPathQuery(String contentPath) {

        String[] pathTokens = contentPath.split("\\.");

        StringBuilder sb = new StringBuilder();
        sb.append(".");
        for (int i = 0; i < pathTokens.length - 1; i++) {
            sb.append("att(n:\"").append(pathTokens[i]).append("\"){");
        }
        sb.append("has(n:\"").append(pathTokens[pathTokens.length - 1]).append("\")");
        for (int i = 0; i < pathTokens.length - 1; i++) {
            sb.append("}");
        }

        return sb.toString();
    }
}

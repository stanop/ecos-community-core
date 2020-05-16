package ru.citeck.ecos.processor;

import java.util.Map;

public interface DataBundleExpander {

    int getOrder();

    boolean isApplicable(Map<String, Object> model);

    Map<String, Object> expandModel(Map<String, Object> model);

}

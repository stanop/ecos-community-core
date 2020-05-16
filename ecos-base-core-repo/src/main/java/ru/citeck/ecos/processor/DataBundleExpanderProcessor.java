package ru.citeck.ecos.processor;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DataBundleExpanderProcessor extends AbstractDataBundleLine {

    private Set<DataBundleExpander> expanders;

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = new HashMap<>(input.needModel());
        if (CollectionUtils.isNotEmpty(expanders)) {
            for (DataBundleExpander expander : expanders) {
                if (expander.isApplicable(model)) {
                    model = expander.expandModel(model);
                }
            }
        }
        return new DataBundle(model);
    }

    @Autowired(required = false)
    public void setFormatters(Set<DataBundleExpander> expanders) {
        this.expanders = new TreeSet<>(Comparator.comparingInt(DataBundleExpander::getOrder));
        this.expanders.addAll(expanders);
    }

}

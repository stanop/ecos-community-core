package ru.citeck.ecos.icase.activity.service.eproc.listeners;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public interface OrderedListener extends Comparable<OrderedListener> {

    int getOrder();

    /**
     * @param another another listener
     * @return result of comparison of orders.<br/>
     * If order values is equals then compares class names by chars.
     */
    @Override
    default int compareTo(@NotNull OrderedListener another) {
        int result = Integer.compare(this.getOrder(), another.getOrder());
        if (result == 0) {
            result = StringUtils.compare(this.getClass().getName(), another.getClass().getName());
        }
        return result;
    }
}

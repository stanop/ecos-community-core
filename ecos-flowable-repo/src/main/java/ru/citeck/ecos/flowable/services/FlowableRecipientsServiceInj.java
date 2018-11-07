package ru.citeck.ecos.flowable.services;

import org.springframework.stereotype.Component;

/**
 * @author Roman Makarskiy
 */
@Component
public class FlowableRecipientsServiceInj extends FlowableRecipientsServiceJS implements
        FlowableEngineProcessService {

    private static final String FLOWABLE_RECIPIENTS_SERVICE_KEY = "flwRecipients";

    @Override
    public String getKey() {
        return FLOWABLE_RECIPIENTS_SERVICE_KEY;
    }
}
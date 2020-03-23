package ru.citeck.ecos.eform;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class EcosFormServiceJS extends AlfrescoScopableProcessorExtension {

    public boolean hasForm(Object record, Object mode) {
        return false;
    }

    public boolean hasForm(Object record) {
        return false;
    }
}

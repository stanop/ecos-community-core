package ru.citeck.ecos.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.barcode.BarcodeAttributeRegistry;
import ru.citeck.ecos.records2.RecordRef;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class BarcodeProcessor extends AbstractDataBundleLine {

    protected BarcodeAttributeRegistry barcodeAttributeRegistry;

    @SuppressWarnings("unchecked")
    protected void handleProperty(Map<String, Object> model) {
        try {
            Object argsObj = model.get("args");
            if (argsObj instanceof HashMap) {
                Map<String, String> args = (HashMap<String, String>) argsObj;
                args.computeIfAbsent("property", e -> {
                    String nodeRef = args.get("nodeRef");
                    RecordRef recordRef = RecordRef.create("", nodeRef);
                    return barcodeAttributeRegistry.getAttribute(recordRef);
                });
            }
        } catch (ClassCastException cce) {
            log.error("Unable to put 'property' in request's params. " + cce.getLocalizedMessage());
        }
    }


    public abstract void setBarcodeAttributeRegistry(BarcodeAttributeRegistry barcodeAttributeRegistry);
}

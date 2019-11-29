package ru.citeck.ecos.barcode;

import com.netflix.servo.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.spring.registry.MappingRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registry for mapping node's value of attribute '_etype' to property.
 */
@Component
public class BarcodeAttributeRegistry {

    private static final String SLASH_DELIMITER = "/";
    private static final String DEFAULT_VALUE = "idocs:barcode";

    private RecordsService recordsService;

    private MappingRegistry<String, String> registry;

    @Autowired
    public BarcodeAttributeRegistry(RecordsService recordsService,
                                    @Qualifier("core.barcode-attribute.type-to-property.mappingRegistry")
                                            MappingRegistry<String, String> registry) {
        this.recordsService = recordsService;
        this.registry = registry;
    }

    public String getAttribute(RecordRef recordRef) {

        String ecosType = recordsService.getAttribute(recordRef, "_etype").asText();

        String result = null;
        while (result == null) {

            if (ecosType.contains(SLASH_DELIMITER)) {
                result = registry.get(ecosType);
            } else {
                result = registry.get(ecosType);
                result = result == null ? DEFAULT_VALUE : result;
            }

            if (result == null) {
                ecosType = this.splitAndGet(ecosType);
            }
        }

        return result;
    }

    private String splitAndGet(String ecosType) {
        if (ecosType.contains(SLASH_DELIMITER)) {
            List<String> parts = new ArrayList<>(Arrays.asList(ecosType.split(SLASH_DELIMITER)));
            parts.remove(parts.size() - 1);
            return Strings.join(SLASH_DELIMITER, parts.iterator());
        }
        return ecosType;
    }
}

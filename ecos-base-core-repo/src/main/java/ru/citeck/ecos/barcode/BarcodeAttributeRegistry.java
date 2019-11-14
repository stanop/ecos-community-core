package ru.citeck.ecos.barcode;

import com.netflix.servo.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class BarcodeAttributeRegistry {

    private static final String SLASH_DELIMITER = "/";
    private static final String DEFAULT_VALUE = "idocs:barcode";

    private RecordsService recordsService;

    private ConcurrentMap<String, String> registry = new ConcurrentHashMap<>();

    @Autowired
    public BarcodeAttributeRegistry(RecordsService recordsService) {
        this.recordsService = recordsService;
    }

    @PostConstruct
    public void init() {
        registry.putIfAbsent("contracts-cat-doctype-contract", "contracts:barcode");
    }

    public String getAttribute(RecordRef recordRef) {

        String ecosType = recordsService.getAttribute(recordRef, "_etype").asText();

        String result = null;
        while (result == null) {

            if (ecosType.contains(SLASH_DELIMITER)) {
                result = registry.get(ecosType);
            } else {
                result = registry.getOrDefault(ecosType, DEFAULT_VALUE);
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

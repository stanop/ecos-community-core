package ru.citeck.ecos.eapps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.apps.EcosAppsFactory;
import ru.citeck.ecos.apps.app.io.EcosAppIO;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EcosAppsFactoryConfig extends EcosAppsFactory {

    @Bean
    @Override
    protected EcosAppIO createEcosAppIO() {
        EcosAppIO io = super.createEcosAppIO();
        Map<String, String> mapping = new HashMap<>();
        mapping.put("form", "ecos-forms");
        io.getReader().setModuleLocations(mapping);
        io.getReader().setModulesRoot(null);
        return io;
    }
}

package ru.citeck.ecos.metarepo;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetaRepoServiceFactoryConfig extends MetaRepoServiceFactory {

    @Bean
    @NotNull
    @Override
    protected MetaRepoDao createMetaRepoDao() {
        return new InMemRepoDao();
    }

    @Bean
    @NotNull
    @Override
    protected EcosMetaRepo createMetaRepository() {
        return super.createMetaRepository();
    }
}

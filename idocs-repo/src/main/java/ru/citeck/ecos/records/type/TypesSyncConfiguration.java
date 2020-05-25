package ru.citeck.ecos.records.type;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.records2.source.dao.local.RemoteSyncRecordsDAO;

@Configuration
public class TypesSyncConfiguration {

    @Bean
    public RemoteSyncRecordsDAO<TypeDto> createRemoteSyncRecordsDao() {
        return new RemoteSyncRecordsDAO<>("emodel/type", TypeDto.class);
    }

    @Bean
    public TypeInfoProvider createInfoProvider() {
        return createRemoteSyncRecordsDao()::getRecord;
    }
}

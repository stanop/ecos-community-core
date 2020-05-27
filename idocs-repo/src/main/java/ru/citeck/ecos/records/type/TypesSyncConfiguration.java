package ru.citeck.ecos.records.type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.commands.dto.CommandResult;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.source.dao.local.RemoteSyncRecordsDAO;

@Slf4j
@Configuration
public class TypesSyncConfiguration {

    @Autowired
    private CommandsService commandsService;

    @Bean
    public RemoteSyncRecordsDAO<TypeDto> createRemoteTypesSyncRecordsDao() {
        return new RemoteSyncRecordsDAO<>("emodel/type", TypeDto.class);
    }

    @Bean
    public RemoteSyncRecordsDAO<NumTemplateDto> createRemoteNumTemplatesSyncRecordsDao() {
        return new RemoteSyncRecordsDAO<>("emodel/num-template", NumTemplateDto.class);
    }

    @Bean
    public TypesManager createInfoProvider() {

        RemoteSyncRecordsDAO<TypeDto> typesDao = createRemoteTypesSyncRecordsDao();
        RemoteSyncRecordsDAO<NumTemplateDto> numTemplatesDao = createRemoteNumTemplatesSyncRecordsDao();

        return new TypesManager() {

            @Override
            public TypeDto getType(RecordRef typeRef) {
                return typesDao.getRecord(typeRef);
            }
            @Override
            public NumTemplateDto getNumTemplate(RecordRef templateRef) {
                return numTemplatesDao.getRecord(templateRef);
            }

            @Override
            public Long getNextNumber(RecordRef templateRef, ObjectData model) {

                Object command = new GetNextNumberCommand(templateRef, model);
                CommandResult numberRes = commandsService.executeSync(command, "emodel");

                Runnable printErrorMsg = () ->
                    log.error("Get next number failed. TemplateRef: " + templateRef + " model: " + model);

                numberRes.throwPrimaryErrorIfNotNull(printErrorMsg);

                if (numberRes.getErrors().size() > 0) {
                    printErrorMsg.run();
                    throw new RuntimeException("Error");
                }

                GetNextNumberResult result = numberRes.getResultAs(GetNextNumberResult.class);

                Long number = result != null ? result.getNumber() : null;
                if (number == null) {
                    throw new IllegalStateException("Number can't be generated");
                }
                return number;
            }
        };
    }
}

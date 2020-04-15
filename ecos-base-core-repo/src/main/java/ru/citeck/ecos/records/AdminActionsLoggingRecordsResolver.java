package ru.citeck.ecos.records;

import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.request.result.RecordsResult;
import ru.citeck.ecos.records2.resolver.LocalRemoteResolver;
import ru.citeck.ecos.records2.resolver.RecordsDAORegistry;
import ru.citeck.ecos.records2.resolver.RecordsResolver;
import ru.citeck.ecos.records2.source.dao.RecordsDAO;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AdminActionsLoggingRecordsResolver implements RecordsResolver, RecordsDAORegistry {

    private RecordsResolver recordsResolver;
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;
    private PersonService personService;

    private LoadingCache<String, Boolean> usersIsAdmin;

    public AdminActionsLoggingRecordsResolver(RecordsResolver recordsResolver, ServiceRegistry serviceRegistry) {
        this.recordsResolver = recordsResolver;
        this.authenticationService = serviceRegistry.getAuthenticationService();
        this.authorityService = serviceRegistry.getAuthorityService();
        this.personService = serviceRegistry.getPersonService();

        usersIsAdmin = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(500)
            .build(CacheLoader.from(this::checkUserAdministration));
    }

    @Override
    public RecordsQueryResult<RecordMeta> queryRecords(RecordsQuery recordsQuery, String s) {

        String userName = authenticationService.getCurrentUserName();

        if (StringUtils.isBlank(userName) || !usersIsAdmin.getUnchecked(userName)) {
            return recordsResolver.queryRecords(recordsQuery, s);
        }

        StringBuilder logInfo = new StringBuilder();

        logInfo.append("User: \"").append(userName).append("\". ")
            .append("Method: \"queryRecords\". ")
            .append("Parameters: [recordsQuery: ").append(Json.getMapper().toString(recordsQuery)).append(", ")
            .append("schema: ").append(Json.getMapper().toString(s)).append("]. \n");

        RecordsQueryResult<RecordMeta> queryResult = recordsResolver.queryRecords(recordsQuery, s);

        logInfo.append("Result: ").append(Json.getMapper().toString(queryResult));

        log.info(logInfo.toString());

        return queryResult;

    }

    @Override
    public RecordsResult<RecordMeta> getMeta(Collection<RecordRef> collection, String s) {

        String userName = authenticationService.getCurrentUserName();

        if (StringUtils.isBlank(userName) || !usersIsAdmin.getUnchecked(userName)) {
            return recordsResolver.getMeta(collection, s);
        }

        StringBuilder logInfo = new StringBuilder();

        logInfo.append("User: \"").append(userName).append("\". ")
            .append("Method: \"getMeta\". ")
            .append("Parameters: [collection: ").append(Json.getMapper().toString(collection)).append(", ")
            .append("schema: ").append(Json.getMapper().toString(s)).append("]. \n");

        RecordsResult<RecordMeta> recordsResult = recordsResolver.getMeta(collection, s);

        logInfo.append("Result: ").append(Json.getMapper().toString(recordsResult));

        log.info(logInfo.toString());

        return recordsResult;
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation recordsMutation) {

        String userName = authenticationService.getCurrentUserName();

        if (StringUtils.isBlank(userName) || !usersIsAdmin.getUnchecked(userName)) {
            return recordsResolver.mutate(recordsMutation);
        }

        StringBuilder logInfo = new StringBuilder();

        logInfo.append("User: \"").append(userName).append("\". ")
            .append("Method: \"mutate\". ")
            .append("Parameters: [recordsMutation: ").append(Json.getMapper().toString(recordsMutation)).append("]. \n");

        RecordsMutResult recordsMutResult = recordsResolver.mutate(recordsMutation);

        logInfo.append("Result: ").append(Json.getMapper().toString(recordsMutResult));

        log.info(logInfo.toString());

        return recordsMutResult;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion recordsDeletion) {

        String userName = authenticationService.getCurrentUserName();

        if (StringUtils.isBlank(userName) || !usersIsAdmin.getUnchecked(userName)) {
            return recordsResolver.delete(recordsDeletion);
        }

        StringBuilder logInfo = new StringBuilder();

        logInfo.append("User: \"").append(userName).append("\". ")
            .append("Method: \"delete\". ")
            .append("Parameters: [recordsDeletion: ").append(Json.getMapper().toString(recordsDeletion)).append("]. \n");

        RecordsDelResult recordsDelResult = recordsResolver.delete(recordsDeletion);

        logInfo.append("Result: ").append(Json.getMapper().toString(recordsDelResult));

        log.info(logInfo.toString());

        return recordsDelResult;
    }

    private Boolean checkUserAdministration(String userName) {
        if (StringUtils.isBlank(userName) || !personService.personExists(userName)) {
            return Boolean.FALSE;
        }
        return authorityService.isAdminAuthority(userName);
    }

    @Override
    public void register(RecordsDAO recordsDAO) {

        if (recordsResolver instanceof LocalRemoteResolver) {
            ((LocalRemoteResolver) recordsResolver).register(recordsDAO);
        } else {
            log.error("Error in register method! RecordsResolver have not found class for cast.");
        }
    }
}

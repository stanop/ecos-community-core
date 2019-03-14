package ru.citeck.ecos.records.source;

import com.fasterxml.jackson.databind.JsonNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeRecord;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsQueryWithMetaLocalDAO;
import ru.citeck.ecos.utils.AuthorityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PeopleRecordsDAO extends LocalRecordsDAO
                              implements RecordsQueryWithMetaLocalDAO<PeopleRecordsDAO.UserValue>,
                                         RecordsMetaLocalDAO<PeopleRecordsDAO.UserValue> {

    public static final String ID = "people";

    private static final String PROP_USER_NAME = "userName";
    private static final String PROP_CM_USER_NAME = "cm:" + PROP_USER_NAME;

    private static final String PROP_FULL_NAME = "fullName";
    private static final String PROP_IS_AVAILABLE = "isAvailable";
    private static final String PROP_IS_MUTABLE = "isMutable";
    private static final String PROP_IS_ADMIN = "isAdmin";
    private static final String PROP_AUTHORITIES = "authorities";

    private static final Log logger = LogFactory.getLog(PeopleRecordsDAO.class);

    private AuthorityUtils authorityUtils;
    private AuthorityService authorityService;
    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private MutableAuthenticationService authenticationService;

    @Autowired
    public PeopleRecordsDAO(AuthorityUtils authorityUtils,
                            AuthorityService authorityService,
                            AlfNodesRecordsDAO alfNodesRecordsDAO,
                            MutableAuthenticationService authenticationService) {
        setId(ID);
        this.authorityUtils = authorityUtils;
        this.authorityService = authorityService;
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
        this.authenticationService = authenticationService;
    }

    @Override
    public List<UserValue> getMetaValues(List<RecordRef> records) {
        return records.stream()
                      .map(r -> new UserValue(r.getId()))
                      .collect(Collectors.toList());
    }

    @Override
    public RecordsQueryResult<UserValue> getMetaValues(RecordsQuery query) {

        if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(query.getLanguage())) {

            RecordsQueryResult<RecordRef> records = alfNodesRecordsDAO.getRecords(query);
            return new RecordsQueryResult<>(records, UserValue::new);
        }

        JsonNode queryNode = query.getQuery();

        if (queryNode.isNull() || queryNode.isMissingNode()) {

            NodeRef ref = authorityService.getAuthorityNodeRef(authenticationService.getCurrentUserName());
            if (ref != null) {
                RecordsQueryResult<UserValue> result = new RecordsQueryResult<>();
                result.addRecord(new UserValue(ref));
                result.setTotalCount(1);
                result.setHasMore(false);
                return result;
            }
        }

        return new RecordsQueryResult<>();
    }

    public class UserValue implements MetaValue {

        private AlfNodeRecord alfNode;
        private String userName;
        private UserAuthorities userAuthorities;

        UserValue(String userName) {
            this.userName = userName;
            NodeRef nodeRef = authorityService.getAuthorityNodeRef(userName);
            alfNode = new AlfNodeRecord(RecordRef.create("", nodeRef.toString()));
        }

        UserValue(NodeRef nodeRef) {
            this.alfNode = new AlfNodeRecord(RecordRef.create("", nodeRef.toString()));
        }

        UserValue(RecordRef recordRef) {
            this.alfNode =  new AlfNodeRecord(recordRef);
        }

        @Override
        public <T extends GqlContext> void init(T context, MetaField metaField) {

            alfNode.init(context, metaField);

            if (userName == null) {
                try {
                    List<? extends MetaValue> attribute = alfNode.getAttribute(PROP_CM_USER_NAME, metaField);
                    userName = attribute.stream()
                                        .findFirst()
                                        .map(MetaValue::getString)
                                        .orElse(null);
                } catch (Exception e) {
                    throw new RuntimeException("Error! " + alfNode.getId(), e);
                }
                if (userName == null) {
                    userName = "";
                }
            }
        }

        @Override
        public String getString() {
            return getId();
        }

        @Override
        public String getId() {
            return userName;
        }

        private UserAuthorities getUserAuthorities() {
            if (userAuthorities == null) {
                userAuthorities = new UserAuthorities(userName);
            }
            return userAuthorities;
        }

        @Override
        public Object getAttribute(String name, MetaField field) {

            switch (name) {
                case PROP_USER_NAME:
                    return userName;
                case PROP_FULL_NAME:
                    return alfNode.getDisplayName();
                case PROP_IS_AVAILABLE:
                    return authenticationService.getAuthenticationEnabled(userName);
                case PROP_IS_MUTABLE:
                    return authenticationService.isAuthenticationMutable(userName);
                case PROP_IS_ADMIN:
                    return authorityService.isAdminAuthority(userName);
                case PROP_AUTHORITIES:
                    return getUserAuthorities();
            }

            return alfNode.getAttribute(name, field);
        }
    }

    private class UserAuthorities implements MetaValue {

        private String userName;
        private Set<String> authorities;

        UserAuthorities(String userName) {
            this.userName = userName;
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public Object getAttribute(String name, MetaField field) {
            switch (name) {
                case "list":
                    return new ArrayList<>(getAuthorities());
            }
            return null;
        }

        private Set<String> getAuthorities() {
            if (authorities == null) {
                authorities = authorityUtils.getUserAuthorities(userName);
            }
            return authorities;
        }

        @Override
        public boolean has(String authority) {
            return getAuthorities().contains(authority);
        }
    }
}

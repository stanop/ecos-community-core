package ru.citeck.ecos.notification;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;

/**
 * @author Roman.Makarskiy on 10/25/2016.
 */
public class DisabledUserNotificationJob extends AbstractScheduledLockedJob {

    private static final String PARAM_NODE_SERVICE = "nodeService";
    private static final String PARAM_SEARCH_SERVICE = "searchService";
    private static final String PARAM_PERSON_SERVICE = "personService";
    private static final String PARAM_NAMESPACE_SERVICE = "namespaceService";
    private static final String PARAM_NOTIFICATION_SENDER = "NotificationSender";
    private static final String PARAM_DOCUMENT_TYPES = "documentTypes";
    private static final String PARAM_SUBJECT_TEMPLATE = "subjectTemplate";
    private static final String PARAM_RECIPIENTS = "recipients";
    private static final String INCLUDE_KEY = "include";
    private static final String EXCLUDE_KEY = "exclude";
    private static final String PARAM_ASPECT_CONDITION = "aspectCondition";
    private static final String PARAM_VERIFY_PERSON_ASSOC = "verifyPerson";
    private static final String PARAM_NOTIFICATION_TYPE = "notificationType";

    private static final Log logger = LogFactory.getLog(DisabledUserNotificationJob.class);

    @Override
    public void executeJob(final JobExecutionContext jobExecutionContext) throws JobExecutionException {

        final Integer doWork = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Integer>() {
            @Override
            public Integer doWork() throws Exception {
                if (logger.isInfoEnabled()) {
                    logger.info("Job start");
                }
                doJob(jobExecutionContext);
                if (logger.isInfoEnabled()) {
                    logger.info("Job end");
                }
                return null;
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void doJob(JobExecutionContext jobExecutionContext) {
        JobDataMap jobData = jobExecutionContext.getJobDetail().getJobDataMap();

        final NodeService nodeService = (NodeService) jobData.get(PARAM_NODE_SERVICE);
        final SearchService searchService = (SearchService) jobData.get(PARAM_SEARCH_SERVICE);
        final PersonService personService = (PersonService) jobData.get(PARAM_PERSON_SERVICE);
        final NamespaceService namespaceService = (NamespaceService) jobData.get(PARAM_NAMESPACE_SERVICE);

        final List<String> documentTypes = (List<String>) jobData.get(PARAM_DOCUMENT_TYPES);
        final String subjectTemplate = (String) jobData.get(PARAM_SUBJECT_TEMPLATE);
        final String notificationType = (String) jobData.get(PARAM_NOTIFICATION_TYPE);
        final ICaseDocumentNotificationSender sender = (ICaseDocumentNotificationSender) jobData.get(PARAM_NOTIFICATION_SENDER);
        final StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

        final String verifyPerson = (String) jobData.get(PARAM_VERIFY_PERSON_ASSOC);
        final Map<String, List<String>> recipients = (Map<String, List<String>>) jobData.get(PARAM_RECIPIENTS);
        final Map<String, List<String>> aspectCondition = (Map<String, List<String>>) jobData.get(PARAM_ASPECT_CONDITION);

        if (recipients == null || documentTypes == null || recipients.isEmpty() || documentTypes.isEmpty()
                || verifyPerson == null || verifyPerson.equals("")) {
            logger.error("Cannot start job, mandatory params is empty.");
            return;
        }

        for (String documentType : documentTypes) {
            int notificationDocCountSend = 0;
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("TYPE:\"").append(documentType).append("\"");
            sbQuery = fillAspectCondition(aspectCondition, sbQuery);
            String query = sbQuery.toString();

            if (logger.isInfoEnabled()) {
                logger.info("Query: " + query);
            }

            SearchParameters sp = new SearchParameters();
            sp.addStore(storeRef);
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query);

            ResultSet results = null;

            try {
                results = searchService.query(sp);

                for (ResultSetRow node : results) {
                    NodeRef verifyUserRef = null;
                    boolean sendNotification = false;
                    NodeRef documentRef = node.getNodeRef();
                    QName assocUserQName = QName.resolveToQName(namespaceService, verifyPerson);
                    List<AssociationRef> verifyUserRefs = nodeService.getTargetAssocs(documentRef, assocUserQName);

                    if (verifyUserRefs.isEmpty()) {
                        sendNotification = true;
                    } else {
                        verifyUserRef = verifyUserRefs.get(0).getTargetRef();

                        if (!nodeService.exists(verifyUserRef) || !userEnabled(verifyUserRef, nodeService, personService)) {
                            sendNotification = true;
                        }
                    }
                    if (sendNotification) {
                        if (logger.isDebugEnabled()) {
                            String verifyUser = verifyUserRef != null ? verifyUserRef.toString()
                                    : "(user deleted or not found)";
                            logger.debug("Found disabled user: " + verifyUser + " in document: " + documentRef);
                        }
                        sender.sendNotification(
                                documentRef,
                                verifyUserRef,
                                recipients,
                                notificationType,
                                subjectTemplate);
                        notificationDocCountSend++;
                    }
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Sent notifications for document type: " + documentType + " count: "
                            + notificationDocCountSend);
                }
            } catch (Exception ex) {
                logger.error("Cannot execute job. Exception message: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                if (results != null) {
                    results.close();
                }
            }
        }


    }

    private StringBuilder fillAspectCondition(Map<String, List<String>> aspectCondition, StringBuilder sbQuery) {
        if (aspectCondition != null && !aspectCondition.isEmpty()) {
            List<String> includeAspects = aspectCondition.get(INCLUDE_KEY);
            List<String> excludeAspects = aspectCondition.get(EXCLUDE_KEY);

            if (includeAspects != null && !includeAspects.isEmpty()) {
                for (String aspect : includeAspects) {
                    sbQuery.append(" AND ASPECT:\"").append(aspect).append("\"");
                }
            }

            if (excludeAspects != null && !excludeAspects.isEmpty()) {
                for (String aspect : excludeAspects) {
                    sbQuery.append(" AND NOT ASPECT:\"").append(aspect).append("\"");
                }
            }
        }
        return sbQuery;
    }

    private boolean userEnabled(NodeRef user, NodeService nodeService, PersonService personService) {
        boolean enabled;
        String userName = (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME);
        enabled = personService.isEnabled(userName);
        if (logger.isDebugEnabled()) {
            logger.debug("Check user: " + userName + " enabled: " + enabled);
        }
        return enabled;
    }
}

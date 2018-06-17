package ru.citeck.ecos.migration;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.common.FillMonthDayPersonPropertyBehaviour;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.webscripts.people.UpcomingBirthdaysGet;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Migration component for processing all users
 * and filling in {@code ecos:birthMonthDay} property
 * This property is used for searching upcoming birthdays.
 * @see UpcomingBirthdaysGet
 */
public class FillMonthDayPersonPropertyModuleComponent extends UsersBatchProcessing {

    private static final List<String> skipUsers = Arrays.asList("System", "guest");

    @Autowired
    private NodeService nodeService;

    @Autowired
    private FillMonthDayPersonPropertyBehaviour monthDayBehaviour;

    @Override
    protected void startProcessing(BatchProcessor<PersonService.PersonInfo> batchProcessor) {
        batchProcessor.process(new FillMonthDayPersonPropertyModuleComponent.FillMonthDayPersonPropertyWorker(nodeService, monthDayBehaviour), true);
    }

    private static class FillMonthDayPersonPropertyWorker extends BatchProcessor.BatchProcessWorkerAdaptor<PersonService.PersonInfo> {

        private NodeService nodeService;
        private FillMonthDayPersonPropertyBehaviour monthDayBehaviour;

        FillMonthDayPersonPropertyWorker(NodeService nodeService,
                                         FillMonthDayPersonPropertyBehaviour monthDayBehaviour) {
            this.monthDayBehaviour = monthDayBehaviour;
            this.nodeService = nodeService;
        }

        @Override
        public void process(PersonService.PersonInfo personInfo) {
            AuthenticationUtil.runAsSystem(() -> {
                String userName = personInfo.getUserName();
                if (!skipUsers.contains(userName)) {
                    NodeRef personRef = personInfo.getNodeRef();
                    Date birthDate = RepoUtils.getProperty(personRef, EcosModel.PROP_BIRTH_DATE, nodeService);
                    monthDayBehaviour.setMonthDayProp(personRef, birthDate);
                }
                return null;
            });
        }
    }
}
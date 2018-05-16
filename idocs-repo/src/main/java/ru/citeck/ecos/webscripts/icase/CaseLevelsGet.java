package ru.citeck.ecos.webscripts.icase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class CaseLevelsGet extends AbstractWebScript {

    private static final String PARAM_NODEREF = "nodeRef";

    private CaseCompletenessService caseCompletenessService;
    private NodeService nodeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String nodeRefParam = req.getParameter(PARAM_NODEREF);

        if (nodeRefParam == null || !NodeRef.isNodeRef(nodeRefParam)) {
            res.getWriter().write("nodeRef is not valid: " + nodeRefParam);
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }

        Response response = evalLevelsData(new NodeRef(nodeRefParam));

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), response);
        res.setStatus(Status.STATUS_OK);
    }

    private Response evalLevelsData(NodeRef caseRef) {

        Response response = new Response();
        response.setNodeRef(caseRef.toString());

        List<Level> levels = new ArrayList<>();

        Set<NodeRef> allLevels = caseCompletenessService.getAllLevels(caseRef);
        Set<NodeRef> currentLevels = caseCompletenessService.getCurrentLevels(caseRef);

        for (NodeRef levelRef : allLevels) {

            Level level = new Level();

            level.setNodeRef(levelRef.toString());
            level.setCompleted(caseCompletenessService.isLevelCompleted(caseRef, levelRef));
            level.setCurrent(currentLevels.contains(levelRef));

            Map<QName, Serializable> props = nodeService.getProperties(levelRef);

            level.setTitle((String) props.get(ContentModel.PROP_TITLE));
            level.setDescription((String) props.get(ContentModel.PROP_DESCRIPTION));
            level.setName((String) props.get(ContentModel.PROP_NAME));

            List<Requirement> requirements = new ArrayList<>();

            Set<NodeRef> reqRefs = caseCompletenessService.getLevelRequirements(levelRef);

            for (NodeRef reqRef : reqRefs) {

                Requirement requirement = new Requirement();
                requirement.setNodeRef(reqRef.toString());

                Map<QName, Serializable> reqProps = nodeService.getProperties(reqRef);
                requirement.setDescription((String) reqProps.get(ContentModel.PROP_DESCRIPTION));
                requirement.setTitle((String) reqProps.get(ContentModel.PROP_TITLE));
                requirement.setName((String) reqProps.get(ContentModel.PROP_NAME));
                requirement.setPassed(caseCompletenessService.isRequirementPassed(caseRef, reqRef));

                Set<NodeRef> elements = caseCompletenessService.getRequirementMatchedElements(caseRef, reqRef);

                List<Match> matchedElements = new ArrayList<>();
                for (NodeRef elementRef : elements) {
                    Match match = new Match();
                    match.setName((String) nodeService.getProperty(elementRef, ContentModel.PROP_NAME));
                    match.setNodeRef(elementRef.toString());
                    matchedElements.add(match);
                }
                requirement.setMatches(matchedElements);
                requirements.add(requirement);
            }

            level.setRequirements(requirements);
            levels.add(level);
        }

        response.setLevels(levels);

        return response;
    }

    @Autowired
    @Qualifier("caseCompletenessServiceCached")
    public void setCaseCompletenessService(CaseCompletenessService caseCompletenessService) {
        this.caseCompletenessService = caseCompletenessService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
    }

    private static class Response {
        @Setter @Getter private String nodeRef;
        @Setter @Getter private List<Level> levels;
    }

    private static class Level {
        @Setter @Getter private String nodeRef;
        @Setter @Getter private String name;
        @Setter @Getter private String title;
        @Setter @Getter private String description;
        @Setter @Getter private boolean current;
        @Setter @Getter private boolean completed;
        @Setter @Getter private List<Requirement> requirements;
    }

    private static class Requirement {
        @Setter @Getter private String nodeRef;
        @Setter @Getter private String name;
        @Setter @Getter private String title;
        @Setter @Getter private String description;
        @Setter @Getter private boolean passed;
        @Setter @Getter private List<Match> matches;
    }

    private static class Match {
        @Setter @Getter private String nodeRef;
        @Setter @Getter private String name;
    }
}

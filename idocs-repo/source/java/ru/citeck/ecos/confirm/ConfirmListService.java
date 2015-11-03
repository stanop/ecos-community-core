/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.confirm;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.*;

@Deprecated
public class ConfirmListService
{

    // web script model fields
	private static final String FIELD_DOCUMENT = "document";
	private static final String FIELD_INITIATOR = "initiator";
	private static final String FIELD_INITIATOR_GROUPS = "initiatorGroups";
	private static final String FIELD_TASKS = "tasks";

	private static final String FIELD_ROLE = "role";
	private static final String FIELD_ASSIGNEE = "assignee";
	private static final String FIELD_DATE = "date";
	private static final String FIELD_COMMENT = "comment";
	private static final String FIELD_OUTCOME = "outcome";
	private static final String FIELD_OUTCOME_LOCALIZED = "outcomeLocalized";

    private NodeService nodeService;
    private WorkflowService workflowService;
    private PersonService personService;
    private DictionaryService dictionaryService;
    private AuthorityService authorityService;

    private Set<String> workflowNames;
	private Set<String> taskNames;
	private boolean onlyLatest = false;

    public Map<String, Object> getModel(NodeRef document) throws Exception {
    	Map<String, Object> model = new HashMap<String, Object>();
    	
    	if(!nodeService.exists(document)) {
    		throw new Exception("There are no document with NodeRef:" + document);
    	}
    	
    	// find document workflows
    	List<WorkflowInstance> workflows = new ArrayList<WorkflowInstance>();
    	workflows.addAll(workflowService.getWorkflowsForContent(document, true));
    	workflows.addAll(workflowService.getWorkflowsForContent(document, false));
    	
    	// find the latest workflow of specified type
    	WorkflowInstance latestWorkflow = null;
    	for(WorkflowInstance workflow : workflows) {
    		if(!workflowNames.contains(workflow.getDefinition().getName())) {
    			continue;
    		}
    		if(latestWorkflow == null || latestWorkflow.getStartDate().before(workflow.getStartDate())) 
    		{
    			latestWorkflow = workflow;
    		}
    	}


    	if(latestWorkflow != null) {
            // get completed tasks of specified type
            Collection<WorkflowTask> tasks = new ArrayList<WorkflowTask>();
            addTasks(tasks, latestWorkflow);
            if(onlyLatest) {
                tasks = filterTasks(tasks);
            }

    	    // construct task list model
        	Object taskListModel = createTaskListModel(tasks);
            model.put(FIELD_TASKS, taskListModel);
            NodeRef initiator = latestWorkflow.getInitiator();
            List<NodeRef> groups = getAllPersonGroups(initiator);
            model.put(FIELD_INITIATOR, initiator);
            model.put(FIELD_INITIATOR_GROUPS, groups);
        }
    	
    	model.put(FIELD_DOCUMENT, document);
    	
		return model;
    	
    }
	
	// filter tasks by assignee
	private Collection<WorkflowTask> filterTasks(Collection<WorkflowTask> tasks) {
		Map<String,WorkflowTask> latestTasks = new HashMap<String,WorkflowTask>();
		for(WorkflowTask task : tasks) {
			String assignee = (String) task.getProperties().get(ContentModel.PROP_OWNER);
			Date completionDate = (Date) task.getProperties().get(WorkflowModel.PROP_COMPLETION_DATE);
			WorkflowTask task2 = latestTasks.get(assignee);
			if(task2 != null) {
				Date completionDate2 = (Date) task2.getProperties().get(WorkflowModel.PROP_COMPLETION_DATE);
				if(completionDate.before(completionDate2)) {
					continue;
				}
			}
			latestTasks.put(assignee, task);
		}
		return latestTasks.values();
	}

	private List<NodeRef> getAllPersonGroups(final NodeRef person) {
		Set<String> personGroups = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Set<String>>() {
			public Set<String> doWork() throws Exception {
				return authorityService.getAuthoritiesForUser((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME));
			}
		});
		List<NodeRef> personGroupRefs = new ArrayList<NodeRef>(personGroups.size());
		for(String groupName : personGroups) {
			NodeRef group = authorityService.getAuthorityNodeRef(groupName);
			if(group != null) {
				personGroupRefs.add(group);
			}
		}
		return personGroupRefs;
	}

	@SuppressWarnings("rawtypes")
	private Object createTaskListModel(Collection<WorkflowTask> tasks) {
		List<Object> taskListModel = new ArrayList<Object>(tasks.size());
		for(WorkflowTask task : tasks) {
			Map<QName, Serializable> taskProperties = task.getProperties();
			Map<String,Object> taskModel = new HashMap<String,Object>();
			
			// role
			Collection pooledActors = (Collection) taskProperties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
			Object role = pooledActors.isEmpty() ? null : pooledActors.iterator().next();
			taskModel.put(FIELD_ROLE, role);
			
			// assignee
			String assigneeUserName = (String) taskProperties.get(ContentModel.PROP_OWNER);
			NodeRef assignee = assigneeUserName != null ?
				personService.getPerson(assigneeUserName) : null;
			taskModel.put(FIELD_ASSIGNEE, assignee);
			
			// completion date
			taskModel.put(FIELD_DATE, taskProperties.get(WorkflowModel.PROP_COMPLETION_DATE));
			
			// comment
			taskModel.put(FIELD_COMMENT, taskProperties.get(WorkflowModel.PROP_COMMENT));
			
			// calculate outcome:
			QName outcomePropertyName = (QName) taskProperties.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
			if(outcomePropertyName == null) {
				outcomePropertyName = WorkflowModel.PROP_OUTCOME;
			}
			String outcome = (String) taskProperties.get(outcomePropertyName);
			taskModel.put(FIELD_OUTCOME, outcome);
			
			// try to localize outcome
			PropertyDefinition outcomePropertyDefinition = dictionaryService.getProperty(outcomePropertyName);
			if(outcomePropertyDefinition != null) {
				List<ConstraintDefinition> constraintDefs = outcomePropertyDefinition.getConstraints();
				for(ConstraintDefinition constraintDef : constraintDefs) {
					Constraint constraint = constraintDef.getConstraint();
					if(constraint instanceof ListOfValuesConstraint) {
						ListOfValuesConstraint listConstraint = (ListOfValuesConstraint) constraint;
						String outcomeLocalized = listConstraint.getDisplayLabel(outcome);
						taskModel.put(FIELD_OUTCOME_LOCALIZED, outcomeLocalized);
					}
				}
			}

			taskListModel.add(taskModel);
		}
		return taskListModel;
	}

	@SuppressWarnings("unused")
	private void addTasks(Collection<WorkflowTask> tasks, List<WorkflowInstance> workflows) {
		for(WorkflowInstance workflow : workflows) {
			addTasks(tasks, workflow);
		}
	}

	private void addTasks(Collection<WorkflowTask> tasks, WorkflowInstance workflow) {
		WorkflowTaskQuery query = new WorkflowTaskQuery();
		query.setProcessId(workflow.getId());
		query.setTaskState(WorkflowTaskState.COMPLETED);
		query.setActive(null);
		List<WorkflowTask> completedTasks = workflowService.queryTasks(query);
		for(WorkflowTask task : completedTasks) {
			if(taskNames.contains(task.getName())) {
				tasks.add(task);
			}
		}
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setWorkflowNames(Set<String> workflowNames) {
		this.workflowNames = workflowNames;
	}

	public void setTaskNames(Set<String> taskNames) {
		this.taskNames = taskNames;
	}

	public void setOnlyLatest(boolean onlyLatest) {
		this.onlyLatest = onlyLatest;
	}

}

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
package ru.citeck.ecos.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.JSONException;
import org.json.JSONObject;

public class LifeCycleDefinition {

    private String docType;
    private String sourceFormat;
    private String title;
    private Boolean enabled;
	private List<LifeCycleState> stateList;
	private List<LifeCycleTransition> transitionList;

	public LifeCycleDefinition() {
		stateList = new ArrayList<LifeCycleState>();
		transitionList = new ArrayList<LifeCycleTransition>();
	}

	public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<LifeCycleState> getStateList() {
		return stateList;
	}

	public void setStateList(List<LifeCycleState> stateList) {
        this.stateList = stateList;
    }

	public List<LifeCycleTransition> getTransitionList() {
		return transitionList;
	}

	public void setTransitionList(List<LifeCycleTransition> transitionList) {
        this.transitionList = transitionList;
    }

	public static class LifeCycleEvent {

		private String eventType;
		private Map<String, String> eventParams;

		public LifeCycleEvent() {
			eventParams = new HashMap<String, String>();
		}

		@SuppressWarnings("rawtypes")
		public LifeCycleEvent(String jsonEvent) {
			this();

			try {
	            JSONObject eventParams = new JSONObject(jsonEvent);
	            Iterator eventKeys = eventParams.keys();

	            while (eventKeys.hasNext()) {
	            	String key = (String) eventKeys.next();

	            	if (key.equals(LifeCycleConstants.EVENT_TYPE))
	            		setEventType(eventParams.getString(key));
	            	else
	            		setEventParam(key, eventParams.getString(key));
	            }
	        } catch (JSONException e) {
	            Logger.getLogger(getClass()).error("Can't parse event JSON", e);
	        }
		}

		public String getEventType() {
			return eventType;
		}

		public void setEventType(String eventType) {
			this.eventType = eventType;
		}

		public Map<String, String> getEventParams() {
            return eventParams;
        }

        public void setEventParams(Map<String, String> eventParams) {
            this.eventParams = eventParams;
        }

		public String getEventParam(String name) {
			if (name != null)
				return eventParams.get(name);

			return null;
		}

		public void setEventParam(String name, String value) {
			if (name != null)
				eventParams.put(name, value);
		}

		public String toJSONString() {
			JSONObject jsonObj = new JSONObject();

			try {
				jsonObj.put(LifeCycleConstants.EVENT_TYPE, getEventType());

				for (String key : eventParams.keySet())
					jsonObj.put(key, getEventParam(key));
			} catch (JSONException e) {
				Logger.getLogger(getClass()).error(e.getMessage(), e);
			}

			return jsonObj.toString();
		}
	}

	static class LifeCycleParamContainer {

	    private String type;
        private Map<String, String> params;

        public LifeCycleParamContainer() {
            params = new HashMap<String, String>();
        }

        public LifeCycleParamContainer(String type) {
            this();
            setType(type);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getParam(String name) {
            if (name != null)
                return params.get(name);

            return null;
        }

        public void setParam(String name, String value) {
            if (name != null)
                params.put(name, value);
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public Map<String, String> getParams() {
            return params;
        }

        @JsonIgnore
        public Set<String> getParamsNames() {
            return params.keySet();
        }

        @JsonIgnore
        public long getParamsCount() {
            return params.size();
        }
	}

	public static class LifeCycleCondition extends LifeCycleParamContainer {
	    public LifeCycleCondition() {
	        super();
	    }

	    public LifeCycleCondition(String type) {
	        super(type);
	    }
    }

	public static class LifeCycleAction extends LifeCycleParamContainer {
	    public LifeCycleAction() {
            super();
        }

        public LifeCycleAction(String type) {
            super(type);
        }
	}

	public static class LifeCycleState {

	    private String id;
		private LifeCycleEvent event;
		private List<LifeCycleAction> startActionList;
		private List<LifeCycleAction> endActionList;

		public LifeCycleState() {
            startActionList = new ArrayList<LifeCycleAction>();
            endActionList = new ArrayList<LifeCycleAction>();
        }

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public LifeCycleEvent getEvent() {
			return event;
		}

		public void setEvent(LifeCycleEvent event) {
			this.event = event;
		}

		public List<LifeCycleAction> getStartActionList() {
			return startActionList;
		}

		public void setStartActionList(List<LifeCycleAction> startActionList) {
            this.startActionList = startActionList;
        }

		public List<LifeCycleAction> getEndActionList() {
			return endActionList;
		}

		public void setEndActionList(List<LifeCycleAction> endActionList) {
            this.endActionList = endActionList;
        }

	}

	public static class LifeCycleTransition {

		private String fromState;
		private LifeCycleEvent event;
		private String toState;
		private List<LifeCycleCondition> conditionList;
		private List<LifeCycleAction> actionList;

		public LifeCycleTransition() {
			conditionList = new ArrayList<LifeCycleCondition>();
			actionList = new ArrayList<LifeCycleAction>();
		}

		public String getFromState() {
			return fromState;
		}

		public void setFromState(String fromState) {
			this.fromState = fromState;
		}

		public LifeCycleEvent getEvent() {
			return event;
		}

		public void setEvent(LifeCycleEvent event) {
			this.event = event;
		}

		public String getToState() {
			return toState;
		}

		public void setToState(String toState) {
			this.toState = toState;
		}

		public List<LifeCycleCondition> getConditionList() {
			return conditionList;
		}

		public void setConditionList(List<LifeCycleCondition> conditionList) {
            this.conditionList = conditionList;
        }

		public List<LifeCycleAction> getActionList() {
			return actionList;
		}

		public void setActionList(List<LifeCycleAction> actionList) {
            this.actionList = actionList;
        }

	}

}

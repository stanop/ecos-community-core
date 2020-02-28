package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"parent", "children"})
public class CaseActivity {

    private String id;
    private int index;
    private String title;
    private String documentId;
    private boolean active;
    private boolean repeatable;
    private State state;
    private CaseActivity parent;
    private Set<CaseActivity> children = new HashSet<>();

    public enum State {

        STARTED("Started"),
        NOT_STARTED("Not started"),
        COMPLETED("Completed");

        private String content;

        State(String content) {
            this.content = content;
        }

        public static State getByContent(String content) {
            for (State state : State.values()) {
                if (StringUtils.equalsIgnoreCase(state.content, content)) {
                    return state;
                }
            }
            throw new IllegalArgumentException("State with content " + content + " not found in enum");
        }

        public String getContent() {
            return content;
        }
    }
}

package ru.citeck.ecos.icase.activity.dto;

import lombok.Data;
import org.alfresco.service.namespace.QName;

import java.util.*;

@Data
public class CaseActivity {

    /*
     * Alfresco NodeRef
     */
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

        public String getContent() {
            return content;
        }
    }


}

package ru.citeck.ecos.icase.completeness;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LevelsNotCompletedException extends RuntimeException {

    private List<NodeRef> levels;

    public LevelsNotCompletedException(Collection<NodeRef> uncompletedLevels, String msg) {
        super(msg);
        levels = new ArrayList<>(uncompletedLevels);
    }

    public List<NodeRef> getLevels() {
        return levels;
    }
}

package ru.citeck.ecos.job.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Simonov
 * @author Roman Makarskiy
 */
public class ExecuteActionJobRegistry {

    private List<ExecuteActionByDateWork> work = new ArrayList<>();

    public void registerWork(ExecuteActionByDateWork work) {
        this.work.add(work);
    }

    public List<ExecuteActionByDateWork> getWorks() {
        return work;
    }
}
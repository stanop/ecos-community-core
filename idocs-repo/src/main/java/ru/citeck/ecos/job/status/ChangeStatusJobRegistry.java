package ru.citeck.ecos.job.status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Simonov
 */
public class ChangeStatusJobRegistry {

    private List<ChangeStatusByDateWork> work = new ArrayList<>();

    public void registerWork(ChangeStatusByDateWork work) {
        this.work.add(work);
    }

    public List<ChangeStatusByDateWork> getWorks() {
        return work;
    }
}
